package io.github.cloudiator.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Node.NodeQueryMessage;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.NodeService;

public class NodeMessageRepository implements MessageRepository<Node> {

  private final NodeService nodeService;
  private final NodeToNodeMessageConverter nodeToNodeMessageConverter = new NodeToNodeMessageConverter();

  @Inject
  public NodeMessageRepository(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Nullable
  @Override
  public Node getById(String userId, String id) {

    final NodeQueryMessage nodeQueryMessage = NodeQueryMessage.newBuilder().setUserId(userId)
        .setNodeId(id)
        .build();

    try {
      return nodeService.queryNodes(nodeQueryMessage).getNodesList().stream().map(
          nodeToNodeMessageConverter::applyBack).collect(StreamUtil.getOnly()).orElse(null);
    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve nodes.", e);
    }
  }

  @Override
  public List<Node> getAll(String userId) {

    final NodeQueryMessage nodeQueryMessage = NodeQueryMessage.newBuilder().setUserId(userId)
        .build();

    try {
      return nodeService.queryNodes(nodeQueryMessage).getNodesList().stream()
          .map(nodeToNodeMessageConverter::applyBack).collect(Collectors
              .toList());
    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve nodes.", e);
    }
  }
}
