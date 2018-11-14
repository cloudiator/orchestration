package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import javax.annotation.Nullable;

class NodePropertiesConverter implements OneWayConverter<NodePropertiesModel, NodeProperties> {

  private final GeoLocationConverter geoLocationConverter = new GeoLocationConverter();

  @Nullable
  @Override
  public NodeProperties apply(@Nullable NodePropertiesModel nodePropertiesModel) {
    if (nodePropertiesModel == null) {
      return null;
    }

    return NodePropertiesBuilder.newBuilder().providerId(nodePropertiesModel.getProviderId())
        .disk(nodePropertiesModel.getDisk())
        .geoLocation(geoLocationConverter.apply(nodePropertiesModel.getGeoLocation()))
        .memory(nodePropertiesModel.getMemory())
        .numberOfCores(nodePropertiesModel.getNumberOfCores())
        .os(nodePropertiesModel.getOperatingSystem()).build();

  }
}
