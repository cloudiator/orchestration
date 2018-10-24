package org.cloudiator.iaas.node.messaging;

import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.persistance.NodeDomainRepository;
import javax.inject.Inject;
import org.cloudiator.iaas.node.NodeDeletionStrategy;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDeleteRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeDeleteRequestListener.class);
  private final MessageInterface messageInterface;
  private final NodeDeletionStrategy nodeDeletionStrategy;
  private final NodeDomainRepository nodeDomainRepository;

  @Inject
  public NodeDeleteRequestListener(MessageInterface messageInterface,
      NodeDeletionStrategy nodeDeletionStrategy,
      NodeDomainRepository nodeDomainRepository) {
    this.messageInterface = messageInterface;
    this.nodeDeletionStrategy = nodeDeletionStrategy;
    this.nodeDomainRepository = nodeDomainRepository;
  }

  @Transactional
  void deleteNode(Node node, String userId) {
    nodeDomainRepository.delete(node.id(), userId);
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeDeleteMessage.class, NodeDeleteMessage.parser(),
        new MessageCallback<NodeDeleteMessage>() {
          @Override
          public void accept(String id, NodeDeleteMessage content) {

            try {

              final String userId = content.getUserId();
              final String nodeId = content.getNodeId();

              Node node = nodeDomainRepository.findByTenantAndId(userId, nodeId);

              if (node == null) {
                messageInterface.reply(NodeDeleteResponseMessage.class, id,
                    Error.newBuilder().setCode(404)
                        .setMessage(String.format("Node with id %s does not exist.", nodeId))
                        .build());
                return;
              }

              boolean b = nodeDeletionStrategy.deleteNode(node, userId);

              if (!b) {
                messageInterface.reply(NodeDeleteResponseMessage.class, id,
                    Error.newBuilder().setCode(500).setMessage("Error while deleting node " + node)
                        .build());
                return;
              }

              deleteNode(node, userId);
              messageInterface.reply(id, NodeDeleteResponseMessage.newBuilder().build());

            } catch (Exception e) {
              LOGGER.error("Unexpected error while deleting node:" + e.getMessage(), e);
              messageInterface.reply(NodeDeleteResponseMessage.class, id,
                  Error.newBuilder().setCode(500)
                      .setMessage("Unexpected error while deleting node:" + e.getMessage())
                      .build());
            }


          }
        });
  }
}
