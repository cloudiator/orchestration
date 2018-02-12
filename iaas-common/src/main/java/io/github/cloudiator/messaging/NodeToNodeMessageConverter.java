package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeType;
import java.util.stream.Collectors;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.NodeProperties.Builder;

public class NodeToNodeMessageConverter implements TwoWayConverter<Node, NodeEntities.Node> {

  private final IpAddressMessageToIpAddress ipAddressConverter = new IpAddressMessageToIpAddress();
  private final NodeTypeToNodeTypeMessage nodeTypeConverter = new NodeTypeToNodeTypeMessage();
  private final NodePropertiesMessageToNodePropertiesConverter nodePropertiesConverter = new NodePropertiesMessageToNodePropertiesConverter();
  private final LoginCredentialMessageToLoginCredentialConverter loginCredentialConverter = new LoginCredentialMessageToLoginCredentialConverter();

  @Override
  public Node applyBack(NodeEntities.Node node) {

    final NodeBuilder nodeBuilder = NodeBuilder.newBuilder().id(node.getId())
        .nodeProperties(nodePropertiesConverter.apply(node.getNodeProperties()))
        .nodeType(nodeTypeConverter.applyBack(node.getNodeType())).ipAddresses(
            node.getIpAddressesList().stream().map(ipAddressConverter)
                .collect(Collectors.toSet()));

    if (node.hasLoginCredential()) {
      nodeBuilder.loginCredential(loginCredentialConverter.apply(node.getLoginCredential()));
    }

    return nodeBuilder.build();
  }

  @Override
  public NodeEntities.Node apply(Node node) {

    final NodeEntities.Node.Builder builder = NodeEntities.Node.newBuilder().setId(node.id())
        .addAllIpAddresses(
            node.ipAddresses().stream().map(ipAddressConverter::applyBack)
                .collect(Collectors.toList()))
        .setNodeProperties(nodePropertiesConverter.applyBack(node.nodeProperties()))
        .setNodeType(nodeTypeConverter.apply(node.type()));

    if (node.loginCredential().isPresent()) {
      builder.setLoginCredential(loginCredentialConverter.applyBack(node.loginCredential().get()));
    }

    return builder.build();
  }

  private static class NodePropertiesMessageToNodePropertiesConverter implements
      TwoWayConverter<NodeEntities.NodeProperties, NodeProperties> {

    private final GeoLocationMessageToGeoLocationConverter geoLocationConverter = new GeoLocationMessageToGeoLocationConverter();
    private final OperatingSystemConverter operatingSystemConverter = new OperatingSystemConverter();

    @Override
    public NodeEntities.NodeProperties applyBack(NodeProperties nodeProperties) {
      final Builder builder = NodeEntities.NodeProperties.newBuilder()
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
    public NodeProperties apply(NodeEntities.NodeProperties nodeProperties) {
      return NodePropertiesBuilder.newBuilder().numberOfCores(nodeProperties.getNumberOfCores())
          .disk(nodeProperties.getDisk())
          .geoLocation(geoLocationConverter.apply(nodeProperties.getGeoLocation()))
          .memory(nodeProperties.getMemory())
          .os(operatingSystemConverter.apply(nodeProperties.getOperationSystem())).build();
    }
  }

  private static class NodeTypeToNodeTypeMessage implements
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
        default:
          throw new AssertionError(String.format("The nodeType %s is not known.", nodeType));
      }
    }
  }
}
