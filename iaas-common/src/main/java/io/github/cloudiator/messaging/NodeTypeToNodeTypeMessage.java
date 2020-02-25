package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.NodeType;
import org.cloudiator.messages.NodeEntities;

public class NodeTypeToNodeTypeMessage implements
    TwoWayConverter<NodeType, NodeEntities.NodeType> {

  @Override
  public NodeType applyBack(NodeEntities.NodeType nodeType) {
    switch (nodeType) {
      case VM:
        return NodeType.VM;
      case BYON:
        return NodeType.BYON;
      case CONTAINER:
        return NodeType.CONTAINER;
      case FAAS:
        return NodeType.FAAS;
      case SIMULATION:
        return NodeType.SIMULATION;
      case UNKNOWN_TYPE:
        return NodeType.UNKOWN;
      case UNRECOGNIZED:
      default:
        throw new AssertionError(String.format("The nodeType %s is not known.", nodeType));
    }
  }

  @Override
  public NodeEntities.NodeType apply(NodeType nodeType) {
    switch (nodeType) {
      case VM:
        return NodeEntities.NodeType.VM;
      case BYON:
        return NodeEntities.NodeType.BYON;
      case UNKOWN:
        return NodeEntities.NodeType.UNKNOWN_TYPE;
      case CONTAINER:
        return NodeEntities.NodeType.CONTAINER;
      case FAAS:
        return NodeEntities.NodeType.FAAS;
      case SIMULATION:
        return NodeEntities.NodeType.SIMULATION;
      default:
        throw new AssertionError(String.format("The nodeType %s is not known.", nodeType));
    }
  }
}
