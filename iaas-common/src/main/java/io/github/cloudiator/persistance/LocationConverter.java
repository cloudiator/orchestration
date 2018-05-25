package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.LocationBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
class LocationConverter implements OneWayConverter<LocationModel, Location> {

  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();

  @Nullable
  @Override
  public Location apply(@Nullable LocationModel locationModel) {
    if (locationModel == null) {
      return null;
    }
    return LocationBuilder.newBuilder().id(locationModel.getCloudUniqueId())
        .providerId(locationModel.getProviderId())
        .name(locationModel.getName()).scope(locationModel.getLocationScope())
        .geoLocation(GEO_LOCATION_CONVERTER.apply(locationModel.getGeoLocationModel()))
        .assignable(locationModel.getAssignable()).parent(apply(locationModel.getParent())).build();
  }
}
