package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.NodeProperties.Builder;


public class NodePropertiesMessageToNodePropertiesConverter implements
    TwoWayConverter<NodeEntities.NodeProperties, NodeProperties> {

  private final GeoLocationMessageToGeoLocationConverter geoLocationConverter = new GeoLocationMessageToGeoLocationConverter();
  private final OperatingSystemConverter operatingSystemConverter = new OperatingSystemConverter();

  @Override
  public NodeEntities.NodeProperties applyBack(NodeProperties nodeProperties) {
    final Builder builder = NodeEntities.NodeProperties.newBuilder()
        .setProviderId(nodeProperties.providerId());

    if (nodeProperties.numberOfCores().isPresent()) {
      builder.setNumberOfCores(nodeProperties.numberOfCores().get());
    }

    if (nodeProperties.memory().isPresent()) {
      builder.setMemory(nodeProperties.memory().get());
    }

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
    return NodePropertiesBuilder.newBuilder().providerId(nodeProperties.getProviderId())
        .numberOfCores(nodeProperties.getNumberOfCores())
        .disk(nodeProperties.getDisk())
        .geoLocation(geoLocationConverter.apply(nodeProperties.getGeoLocation()))
        .memory(nodeProperties.getMemory())
        .os(operatingSystemConverter.apply(nodeProperties.getOperationSystem())).build();
  }
}
