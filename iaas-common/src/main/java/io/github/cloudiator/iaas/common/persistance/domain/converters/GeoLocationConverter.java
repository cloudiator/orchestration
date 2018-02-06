package io.github.cloudiator.iaas.common.persistance.domain.converters;

import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocationBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.iaas.common.persistance.entities.GeoLocationModel;
import javax.annotation.Nullable;

public class GeoLocationConverter implements OneWayConverter<GeoLocationModel, GeoLocation> {

  @Nullable
  @Override
  public GeoLocation apply(@Nullable GeoLocationModel geoLocationModel) {

    if (geoLocationModel == null) {
      return null;
    }

    return GeoLocationBuilder.newBuilder().city(geoLocationModel.getCity())
        .country(geoLocationModel.getCountry()).latitude(geoLocationModel.getLocationLatitude())
        .longitude(geoLocationModel.getLocationLongitude()).build();
  }
}
