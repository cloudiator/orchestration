package io.github.cloudiator.iaas.discovery.converters;

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

  @Override
  public IaasEntities.Location applyBack(Location location) {
    if (location == null) {
      return null;
    }
    Builder builder = IaasEntities.Location.newBuilder().setId(location.id())
        .setName(location.name())
        .setLocationScope(locationScopeConverter.applyBack(location.locationScope()))
        .setIsAssignable(location.isAssignable());

    if (location.parent().isPresent()) {
      builder.setParent(applyBack(location.parent().get()));
    }

    return builder.build();
  }

  @Override
  public Location apply(IaasEntities.Location location) {
    if (location == null) {
      return null;
    }
    return LocationBuilder.newBuilder().id(location.getId()).name(location.getName())
        .parent(apply(location)).assignable(location.getIsAssignable())
        .scope(locationScopeConverter.apply(location.getLocationScope())).build();
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
          return CommonEntities.LocationScope.REGION;
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
