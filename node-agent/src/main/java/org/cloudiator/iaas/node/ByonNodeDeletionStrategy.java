package org.cloudiator.iaas.node;

import static jersey.repackaged.com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeToNodeConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Byon.ByonNodeDeleteRequestMessage;
import org.cloudiator.messages.Byon.ByonNodeDeletedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ByonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeDeletionStrategy implements NodeDeletionStrategy {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonNodeDeletionStrategy.class);
  private final ByonService byonService;

  @Inject
  public ByonNodeDeletionStrategy(
      ByonService byonService) {
    this.byonService = byonService;
  }

  @Override
  public boolean supportsNode(Node node) {
    return node.type().equals(NodeType.BYON);
  }

  @Override
  public boolean deleteNode(Node node) {
      Node deletedNode = setDeleted(node);
      ByonNode byonNode = ByonNodeToNodeConverter.INSTANCE.applyBack(deletedNode);
      ByonNodeDeleteRequestMessage byonNodeDeleteRequestMessage = ByonNodeDeleteRequestMessage
          .newBuilder().setByonNode(ByonToByonMessageConverter.INSTANCE.apply(byonNode)).build();

      final SettableFutureResponseCallback<ByonNodeDeletedResponse, ByonNodeDeletedResponse>
          byonFuture = SettableFutureResponseCallback.create();

      checkState(byonNode.id() != null, "No id is present on byon. Can not delete");

      byonService.createByonPersistDelAsync(byonNodeDeleteRequestMessage, byonFuture);

      try {
        byonFuture.get();
        return true;
      } catch (InterruptedException e) {
        LOGGER.error(String.format("%s got interrupted while waiting for response.", this));
        return false;
      } catch (ExecutionException e) {
        LOGGER.error(String
            .format("Deletion of byon %s failed, as byon delete request failed with %s.",
                node, e.getCause().getMessage()), e);
        return false;
      }
  }

  private Node setDeleted(Node node) {
    return NodeBuilder.newBuilder()
        .name(node.name())
        .nodeType(node.type())
        .originId(node.originId().orElse(null))
        // set deleted
        .state(NodeState.DELETED)
        .userId(node.userId())
        .diagnostic(node.diagnostic().orElse(null))
        .id(node.id())
        .ipAddresses(node.ipAddresses())
        .nodeCandidate(node.nodeCandidate().orElse(null))
        .loginCredential(node.loginCredential().orElse(null))
        .reason(node.reason().orElse(null))
        .nodeProperties(node.nodeProperties())
        .build();
  }
}
