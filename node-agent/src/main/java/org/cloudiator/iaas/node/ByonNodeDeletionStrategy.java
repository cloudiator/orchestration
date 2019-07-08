package org.cloudiator.iaas.node;

import static jersey.repackaged.com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeToNodeConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.messaging.NodePropertiesMessageToNodePropertiesConverter;
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
  private static final NodePropertiesMessageToNodePropertiesConverter NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();

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
      ByonNodeDeleteRequestMessage byonNodeDeleteRequestMessage = ByonNodeDeleteRequestMessage
          .newBuilder().setUserId(deletedNode.userId())
          .setProperties(NODE_PROPERTIES_CONVERTER.applyBack(buildProperties(deletedNode)))
          .setAllocated(false).build();

      final SettableFutureResponseCallback<ByonNodeDeletedResponse, ByonNodeDeletedResponse>
          byonFuture = SettableFutureResponseCallback.create();

      checkState(deletedNode.id() != null, "No id is present on byon. Can not delete");

      byonService.createByonPersistDelAsync(byonNodeDeleteRequestMessage, byonFuture);

      try {
        ByonNodeDeletedResponse response = byonFuture.get();
        final ByonNode deletedNodeResponded = ByonToByonMessageConverter.INSTANCE.applyBack(response.getNode());
        if(!consistencyCheck(deletedNode, deletedNodeResponded)) {
          return false;
        }
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

  private static boolean consistencyCheck(Node deletedNode, ByonNode deletedNodeResponded) {
    if(deletedNode == deletedNodeResponded) {
      return true;
    }

    return false;
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
        .nodeCandidate(node.nodeCandidate().orElse(null))
        .loginCredential(node.loginCredential().orElse(null))
        .reason(node.reason().orElse(null))
        .nodeProperties(node.nodeProperties())
        .build();
  }

  private NodeProperties buildProperties(Node deletedNode) {
    return NodePropertiesBuilder.newBuilder()
        .providerId(deletedNode.nodeProperties().providerId())
        .numberOfCores(deletedNode.nodeProperties().numberOfCores().orElse(null))
        .memory(deletedNode.nodeProperties().memory().orElse(null))
        .disk(deletedNode.nodeProperties().disk().orElse(null))
        .os(deletedNode.nodeProperties().operatingSystem().orElse(null))
        .geoLocation(deletedNode.nodeProperties().geoLocation().orElse(null))
        .build();
  }
}
