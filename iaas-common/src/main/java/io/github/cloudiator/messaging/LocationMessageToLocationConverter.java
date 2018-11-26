/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

  public static final LocationMessageToLocationConverter INSTANCE = new LocationMessageToLocationConverter();
  private final LocationScopeMessageToLocationScopeConverter locationScopeConverter = new LocationScopeMessageToLocationScopeConverter();
  private final GeoLocationMessageToGeoLocationConverter geoLocationConverter = new GeoLocationMessageToGeoLocationConverter();

  private LocationMessageToLocationConverter() {
  }

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
