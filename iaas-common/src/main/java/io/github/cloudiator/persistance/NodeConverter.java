package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class NodeConverter implements OneWayConverter<NodeModel, Node> {

  private final NodePropertiesConverter nodePropertiesConverter = new NodePropertiesConverter();
  private final LoginCredentialConverter loginCredentialConverter = new LoginCredentialConverter();
  private final IpAddressConverter ipAddressConverter = new IpAddressConverter();

  @Nullable
  @Override
  public Node apply(@Nullable NodeModel nodeModel) {
    if (nodeModel == null) {
      return null;
    }

    NodeBuilder nodeBuilder = NodeBuilder.newBuilder()
        .id(nodeModel.getDomainId())
        .name(nodeModel.getName())
        .loginCredential(loginCredentialConverter.apply(nodeModel.getLoginCredential()))
        .nodeProperties(nodePropertiesConverter.apply(nodeModel.getNodeProperties()))
        .nodeType(nodeModel.getType());

    nodeBuilder.ipAddresses(
        nodeModel.ipAddresses().stream().map(ipAddressConverter).collect(Collectors.toSet()));

    return nodeBuilder.build();

  }
}
