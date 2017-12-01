package io.github.cloudiator.iaas.common.messaging.converters;

import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;

public class GeoLocationMessageToGeoLocationConverter implements TwoWayConverter<IaasEntities.GeoLocation,GeoLocation>{

  @Override
  public IaasEntities.GeoLocation applyBack(GeoLocation geoLocation) {
    return null;
  }

  @Override
  public GeoLocation apply(IaasEntities.GeoLocation geoLocation) {
    return null;
  }
}
