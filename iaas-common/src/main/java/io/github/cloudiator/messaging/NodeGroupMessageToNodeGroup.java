package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.domain.NodeGroups;
import java.util.LinkedList;
import java.util.List;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.NodeGroup.Builder;

public class NodeGroupMessageToNodeGroup implements
    TwoWayConverter<NodeEntities.NodeGroup, NodeGroup> {

  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = new NodeToNodeMessageConverter();

  @Override
  public NodeEntities.NodeGroup applyBack(NodeGroup nodeGroup) {

    final Builder builder = NodeEntities.NodeGroup.newBuilder();
    for (final Node node : nodeGroup.getNodes()) {
      builder.addNodes(NODE_MESSAGE_CONVERTER.apply(node));
    }
    builder.setId(nodeGroup.id());

    return builder.build();
  }

  @Override
  public NodeGroup apply(NodeEntities.NodeGroup nodeGroup) {

    List<Node> nodes = new LinkedList<>();
    for (NodeEntities.Node node : nodeGroup.getNodesList()) {
      nodes.add(NODE_MESSAGE_CONVERTER.applyBack(node));
    }

    return NodeGroups.of(nodeGroup.getId(), nodes);
  }
}
