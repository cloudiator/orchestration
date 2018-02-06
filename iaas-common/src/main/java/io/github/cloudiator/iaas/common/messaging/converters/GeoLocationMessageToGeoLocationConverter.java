package io.github.cloudiator.iaas.common.messaging.converters;

import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocationBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import java.math.BigDecimal;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.GeoLocation.Builder;

public class GeoLocationMessageToGeoLocationConverter implements
    TwoWayConverter<IaasEntities.GeoLocation, GeoLocation> {

  @Override
  public IaasEntities.GeoLocation applyBack(GeoLocation geoLocation) {
    final Builder builder = IaasEntities.GeoLocation.newBuilder();

    if (geoLocation.city() != null) {
      builder.setCity(geoLocation.city());
    }
    if (geoLocation.country() != null) {
      builder.setCountry(geoLocation.country());
    }
    if (geoLocation.latitude() != null) {
      builder.setLatitude(geoLocation.latitude().doubleValue());
    }
    if (geoLocation.longitude() != null) {
      builder.setLongitude(geoLocation.longitude().doubleValue());
    }

    return builder.build();
  }

  @Override
  public GeoLocation apply(IaasEntities.GeoLocation geoLocation) {
    final GeoLocationBuilder geoLocationBuilder = GeoLocationBuilder.newBuilder();

    if (!geoLocation.getCity().equals("")) {
      geoLocationBuilder.city(geoLocation.getCity());
    }
    if (!geoLocation.getCountry().equals("")) {
      geoLocationBuilder.country(geoLocation.getCountry());
    }
    if (geoLocation.getLatitude() != 0) {
      geoLocationBuilder.latitude(BigDecimal.valueOf(geoLocation.getLatitude()));
    }
    if (geoLocation.getLongitude() != 0) {
      geoLocationBuilder.longitude(BigDecimal.valueOf(geoLocation.getLongitude()));
    }

    return geoLocationBuilder.build();
  }
}
