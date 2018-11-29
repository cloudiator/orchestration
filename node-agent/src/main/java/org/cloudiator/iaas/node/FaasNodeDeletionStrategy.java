package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeType;
import org.cloudiator.messages.Function;
import org.cloudiator.messages.Function.FunctionDeletedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.FunctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class FaasNodeDeletionStrategy implements NodeDeletionStrategy {

  private final FunctionService functionService;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(FaasNodeDeletionStrategy.class);

  @Inject
  public FaasNodeDeletionStrategy(FunctionService functionService) {
    this.functionService = functionService;
  }

  @Override
  public boolean supportsNode(Node node) {
    return node.type().equals(NodeType.FAAS);
  }

  @Override
  public boolean deleteNode(Node node, String userId) {

    SettableFutureResponseCallback<FunctionDeletedResponse, FunctionDeletedResponse> future =
        SettableFutureResponseCallback.create();
    functionService.deleteFuntionAsync(
        Function.DeleteFunctionRequestMessage.newBuilder()
            .setFunctionId(node.id()).setUserId(userId).build(), future);

    try {
      future.get();
      return true;
    } catch (InterruptedException e) {
      LOGGER.error(String.format("%s got interrupted while waiting for response.", this));
      return false;
    } catch (ExecutionException e) {
      LOGGER.error(String
          .format("Deletion of node %s failed, as function delete request failed with %s.",
              node, e.getCause().getMessage()), e);
      return false;
    }
  }
}
