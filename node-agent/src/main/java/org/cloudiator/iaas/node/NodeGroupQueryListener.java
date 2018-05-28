package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import io.github.cloudiator.persistance.NodeDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeGroupQueryMessage;
import org.cloudiator.messages.Node.NodeGroupQueryResponse;
import org.cloudiator.messages.Node.NodeGroupQueryResponse.Builder;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeGroupQueryListener implements Runnable {

  private final MessageInterface messageInterface;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeGroupQueryListener.class);
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeGroupMessageToNodeGroup nodeGroupMessageToNodeGroup = new NodeGroupMessageToNodeGroup();

  @Inject
  public NodeGroupQueryListener(MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository) {
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeGroupQueryMessage.class, NodeGroupQueryMessage.parser(),
        new MessageCallback<NodeGroupQueryMessage>() {
          @Override
          public void accept(String id, NodeGroupQueryMessage content) {
            try {
              if (content.getUserId() == null || content.getUserId().isEmpty()) {
                messageInterface.reply(id,
                    Error.newBuilder().setCode(500).setMessage("No userId was provided.").build());
              }

              final NodeGroupQueryResponse nodeGroupQueryResponse = handleResponse(content);
              messageInterface.reply(id, nodeGroupQueryResponse);

            } catch (Exception e) {
              LOGGER.error("Unexpected exception while querying node groups.", e);
              messageInterface.reply(id, Error.newBuilder().setCode(500).setMessage(
                  "Unexpected exception while querying node groups. Error was " + e.getMessage())
                  .build());
            }
          }
        });
  }

  @Transactional
  private NodeGroupQueryResponse handleResponse(NodeGroupQueryMessage nodeGroupQueryMessage) {
    final String userId = nodeGroupQueryMessage.getUserId();
    final String id = nodeGroupQueryMessage.getNodeGroupId();

    if (id == null || id.isEmpty()) {
      return handleMultiple(userId);
    }
    return handleSingle(userId, id);


  }

  private NodeGroupQueryResponse handleSingle(String userId, String id) {

    final NodeGroup group = nodeDomainRepository.findGroupByTenantAndId(userId, id);
    if (group == null) {
      return NodeGroupQueryResponse.newBuilder().build();
    }

    return NodeGroupQueryResponse.newBuilder().addNodeGroups(nodeGroupMessageToNodeGroup
        .applyBack(group)).build();
  }

  private NodeGroupQueryResponse handleMultiple(String userId) {
    final Builder builder = NodeGroupQueryResponse.newBuilder();

    nodeDomainRepository.findGroupsByTenant(userId).stream()
        .map(nodeGroupMessageToNodeGroup::applyBack).forEach(builder::addNodeGroups);

    return builder.build();

  }

}
