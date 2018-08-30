package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.domain.LocationScope;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.LocationBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Location.Builder;

/**
 * Created by daniel on 07.06.17.
 */
public class LocationMessageToLocationConverter implements
    TwoWayConverter<IaasEntities.Location, Location> {

  private final LocationScopeMessageToLocationScopeConverter locationScopeConverter = new LocationScopeMessageToLocationScopeConverter();
  private final GeoLocationMessageToGeoLocationConverter geoLocationConverter = new GeoLocationMessageToGeoLocationConverter();

  public static final LocationMessageToLocationConverter INSTANCE = new LocationMessageToLocationConverter();

  private LocationMessageToLocationConverter() {}

  @Override
  public IaasEntities.Location applyBack(Location location) {
    if (location == null) {
      return null;
    }
    Builder builder = IaasEntities.Location.newBuilder().setId(location.id())
        .setProviderId(location.providerId())
        .setName(location.name())
        .setLocationScope(locationScopeConverter.applyBack(location.locationScope()))
        .setIsAssignable(location.isAssignable());

    if (location.parent().isPresent()) {
      builder.setParent(applyBack(location.parent().get()));
    }

    if (location.geoLocation().isPresent()) {
      builder.setGeoLocation(geoLocationConverter.applyBack(location.geoLocation().get()));
    }

    return builder.build();
  }

  @Override
  public Location apply(IaasEntities.Location location) {
    if (location == null) {
      return null;
    }
    final LocationBuilder locationBuilder = LocationBuilder.newBuilder().id(location.getId())
        .providerId(location.getProviderId())
        .name(location.getName())
        .assignable(location.getIsAssignable())
        .scope(locationScopeConverter.apply(location.getLocationScope()))
        .geoLocation(geoLocationConverter.apply(location.getGeoLocation()));

    if (location.hasParent()) {
      locationBuilder.parent(apply(location.getParent()));
    }

    return locationBuilder.build();

  }

  private static class LocationScopeMessageToLocationScopeConverter implements
      TwoWayConverter<CommonEntities.LocationScope, LocationScope> {

    @Override
    public CommonEntities.LocationScope applyBack(LocationScope locationScope) {
      switch (locationScope) {
        case ZONE:
          return CommonEntities.LocationScope.ZONE;
        case HOST:
          return CommonEntities.LocationScope.HOST;
        case REGION:
          return CommonEntities.LocationScope.REGION;
        case PROVIDER:
          return CommonEntities.LocationScope.PROVIDER;
        default:
          throw new AssertionError(String.format("Unknown location scope %s.", locationScope));
      }
    }

    @Override
    public LocationScope apply(CommonEntities.LocationScope locationScope) {
      switch (locationScope) {
        case PROVIDER:
          return LocationScope.PROVIDER;
        case REGION:
          return LocationScope.REGION;
        case HOST:
          return LocationScope.HOST;
        case ZONE:
          return LocationScope.ZONE;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(String.format("Unknown location scope %s.", locationScope));
      }
    }
  }
}
