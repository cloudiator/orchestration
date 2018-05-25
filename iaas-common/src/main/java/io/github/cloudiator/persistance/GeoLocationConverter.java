package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocationBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class GeoLocationConverter implements OneWayConverter<GeoLocationModel, GeoLocation> {

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
