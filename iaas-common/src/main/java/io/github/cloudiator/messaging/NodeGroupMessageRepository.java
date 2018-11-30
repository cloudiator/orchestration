package io.github.cloudiator.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.domain.NodeGroup;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Node.NodeGroupQueryMessage;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.NodeService;

/**
 * Created by Daniel Seybold on 26.11.2018.
 */
public class NodeGroupMessageRepository implements MessageRepository<NodeGroup> {

  private final NodeService nodeService;

  NodeGroupMessageToNodeGroup nodeGroupMessageToNodeGroup = new NodeGroupMessageToNodeGroup();

  @Inject
  public NodeGroupMessageRepository(NodeService nodeService){
    this.nodeService = nodeService;
  }


  @Nullable
  @Override
  public NodeGroup getById(String userId, String id) {

    final NodeGroupQueryMessage nodeGroupQueryMessage = NodeGroupQueryMessage.newBuilder()
        .setUserId(userId).setNodeGroupId(id).build();

    try {
      return nodeService.queryNodeGroups(nodeGroupQueryMessage).getNodeGroupsList().stream().map(
          nodeGroupMessageToNodeGroup::apply).collect(StreamUtil.getOnly()).orElse(null);

    } catch (ResponseException e) {
        throw new IllegalStateException("Could not retrieve nodes.", e);
      }


  }

  @Override
  public List<NodeGroup> getAll(String userId) {
    final NodeGroupQueryMessage nodeGroupQueryMessage = NodeGroupQueryMessage.newBuilder()
        .setUserId(userId).build();

    try {
      return nodeService.queryNodeGroups(nodeGroupQueryMessage).getNodeGroupsList().stream().map(
          nodeGroupMessageToNodeGroup::apply).collect(Collectors
          .toList());

    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve nodes.", e);
    }

  }
}
