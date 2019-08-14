package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.NodeState;
import org.cloudiator.messages.NodeEntities;

public class NodeStateConverter implements
    TwoWayConverter<NodeState, NodeEntities.NodeState> {

  public NodeStateConverter() {
  }

  @Override
  public NodeState applyBack(NodeEntities.NodeState nodeState) {
    switch (nodeState) {
      case NODE_STATE_PENDING:
        return NodeState.PENDING;
      case NODE_STATE_RUNNING:
        return NodeState.RUNNING;
      case NODE_STATE_ERROR:
        return NodeState.ERROR;
      case NODE_STATE_DELETED:
        return NodeState.DELETED;
      case UNRECOGNIZED:
      default:
        throw new AssertionError("Unknown nodeState " + nodeState);
    }
  }

  @Override
  public NodeEntities.NodeState apply(NodeState nodeState) {

    switch (nodeState) {
      case PENDING:
        return NodeEntities.NodeState.NODE_STATE_PENDING;
      case ERROR:
        return NodeEntities.NodeState.NODE_STATE_ERROR;
      case RUNNING:
        return NodeEntities.NodeState.NODE_STATE_RUNNING;
      case DELETED:
        return NodeEntities.NodeState.NODE_STATE_DELETED;
      default:
        throw new AssertionError("Unknown node state " + nodeState);
    }
  }
}
