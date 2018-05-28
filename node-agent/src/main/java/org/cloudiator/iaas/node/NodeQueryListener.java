package org.cloudiator.iaas.node;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.NodeDomainRepository;
import java.util.List;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeQueryMessage;
import org.cloudiator.messages.Node.NodeQueryResponse;
import org.cloudiator.messages.Node.NodeQueryResponse.Builder;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeQueryListener implements Runnable {

  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeToNodeMessageConverter nodeToNodeMessageConverter = new NodeToNodeMessageConverter();
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeQueryListener.class);

  @Inject
  public NodeQueryListener(MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository) {
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeQueryMessage.class, NodeQueryMessage.parser(),
        new MessageCallback<NodeQueryMessage>() {
          @Override
          public void accept(String id, NodeQueryMessage content) {
            try {

              if (content.getUserId() == null || content.getUserId().isEmpty()) {
                messageInterface.reply(id,
                    Error.newBuilder().setCode(500).setMessage("No userId was provided").build());
              }

              final NodeQueryResponse nodeQueryResponse = handleResponse(content.getNodeId(),
                  content.getUserId());
              messageInterface.reply(id, nodeQueryResponse);
            } catch (Exception e) {
              LOGGER.error("Unexpected error while querying nodes", e);
              messageInterface.reply(id, Error.newBuilder().setCode(500).setMessage(
                  "Unexpected error while querying nodes. Message was " + e.getMessage()).build());
            }

          }
        });
  }

  private NodeQueryResponse handleResponse(@Nullable String id, String userId) {
    checkNotNull(userId, "userId is null");
    if (id == null || id.isEmpty()) {
      return handleMultiple(userId);
    }
    return handleSingle(id, userId);
  }

  private NodeQueryResponse handleSingle(String id, String userId) {
    checkNotNull(id, "id is null");

    final Node node = nodeDomainRepository.findByTenantAndId(userId, id);
    if (node == null) {
      return NodeQueryResponse.newBuilder().build();
    }
    return NodeQueryResponse.newBuilder().addNodes(nodeToNodeMessageConverter.apply(node)).build();
  }

  private NodeQueryResponse handleMultiple(String userId) {

    final List<Node> nodes = nodeDomainRepository.findByTenant(userId);

    final Builder builder = NodeQueryResponse.newBuilder();

    nodes.stream().map(nodeToNodeMessageConverter).forEach(
        builder::addNodes);

    return builder.build();
  }


}
