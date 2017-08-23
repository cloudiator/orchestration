package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.iaas.common.domain.Node;
import io.github.cloudiator.iaas.common.domain.NodeBuilder;
import io.github.cloudiator.iaas.common.domain.NodeProperties;
import io.github.cloudiator.iaas.common.domain.NodePropertiesBuilder;
import io.github.cloudiator.iaas.common.domain.NodeType;
import java.util.stream.Collectors;
import org.cloudiator.messages.NodeOuterClass;
import org.cloudiator.messages.NodeOuterClass.NodeProperties.Builder;

public class NodeToNodeMessage implements TwoWayConverter<Node, NodeOuterClass.Node> {

  private final IpAddressMessageToIpAddress ipAddressConverter = new IpAddressMessageToIpAddress();
  private final NodeTypeToNodeTypeMessage nodeTypeConverter = new NodeTypeToNodeTypeMessage();

  @Override
  public Node applyBack(NodeOuterClass.Node node) {

    return NodeBuilder.newBuilder().ipAddresses(node.getIpAddressesList().stream().map(
        ipAddressConverter).collect(
        Collectors.toSet())).nodeType(nodeTypeConverter.applyBack(node.getNodeType())).build();
  }

  @Override
  public NodeOuterClass.Node apply(Node node) {
    return null;
  }

  private static class NodePropertiesMessageToNodePropertiesConverter implements
      TwoWayConverter<NodeOuterClass.NodeProperties, NodeProperties> {

    private final GeoLocationMessageToGeoLocationConverter geoLocationConverter = new GeoLocationMessageToGeoLocationConverter();
    private final OperatingSystemConverter operatingSystemConverter = new OperatingSystemConverter();

    @Override
    public NodeOuterClass.NodeProperties applyBack(NodeProperties nodeProperties) {
      final Builder builder = NodeOuterClass.NodeProperties.newBuilder()
          .setNumberOfCores(nodeProperties.numberOfCores()).setMemory(nodeProperties.memory());

      if (nodeProperties.geoLocation().isPresent()) {
        builder.setGeoLocation(geoLocationConverter.applyBack(nodeProperties.geoLocation().get()));
      }

      if (nodeProperties.operatingSystem().isPresent()) {
        builder.setOperationSystem(
            operatingSystemConverter.applyBack(nodeProperties.operatingSystem().get()));
      }

      if (nodeProperties.disk().isPresent()) {
        builder.setDisk(nodeProperties.disk().get());
      }

      return builder.build();

    }

    @Override
    public NodeProperties apply(NodeOuterClass.NodeProperties nodeProperties) {
      return NodePropertiesBuilder.newBuilder().numberOfCores(nodeProperties.getNumberOfCores())
          .disk(nodeProperties.getDisk())
          .geoLocation(geoLocationConverter.apply(nodeProperties.getGeoLocation()))
          .memory(nodeProperties.getMemory())
          .os(operatingSystemConverter.apply(nodeProperties.getOperationSystem())).build();
    }
  }

  private static class NodeTypeToNodeTypeMessage implements
      TwoWayConverter<NodeType, NodeOuterClass.NodeType> {

    @Override
    public NodeType applyBack(NodeOuterClass.NodeType nodeType) {
      switch (nodeType) {
        case VM:
          return NodeType.VM;
        case BYON:
          return NodeType.BYON;
        case CONTAINER:
          return NodeType.CONTAINER;
        case UNKNOWN_TYPE:
          return NodeType.UNKOWN;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(String.format("The nodeType %s is not known.", nodeType));
      }
    }

    @Override
    public NodeOuterClass.NodeType apply(NodeType nodeType) {
      switch (nodeType) {
        case VM:
          return NodeOuterClass.NodeType.VM;
        case BYON:
          return NodeOuterClass.NodeType.BYON;
        case UNKOWN:
          return NodeOuterClass.NodeType.UNKNOWN_TYPE;
        case CONTAINER:
          return NodeOuterClass.NodeType.CONTAINER;
        default:
          throw new AssertionError(String.format("The nodeType %s is not known.", nodeType));
      }
    }
  }
}
