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

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.LocationBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.DiscoveredLocation;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
class LocationConverter implements OneWayConverter<LocationModel, Location> {

  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();

  @Nullable
  @Override
  public DiscoveredLocation apply(@Nullable LocationModel locationModel) {
    if (locationModel == null) {
      return null;
    }
    return new DiscoveredLocation(LocationBuilder.newBuilder().id(locationModel.getCloudUniqueId())
        .providerId(locationModel.getProviderId())
        .name(locationModel.getName()).scope(locationModel.getLocationScope())
        .geoLocation(GEO_LOCATION_CONVERTER.apply(locationModel.getGeoLocationModel()))
        .assignable(locationModel.getAssignable()).parent(apply(locationModel.getParent())).build(),
        locationModel.getState(), locationModel.getTenant().getUserId());
  }
}
