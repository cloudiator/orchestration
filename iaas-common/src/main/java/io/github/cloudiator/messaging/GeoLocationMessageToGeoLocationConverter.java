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
    if (geoLocation == null) {
      return null;
    }
    final Builder builder = IaasEntities.GeoLocation.newBuilder();

    if (geoLocation.city().isPresent()) {
      builder.setCity(geoLocation.city().get());
    }
    if (geoLocation.country().isPresent()) {
      builder.setCountry(geoLocation.country().get());
    }
    if (geoLocation.latitude().isPresent()) {
      builder.setLatitude(geoLocation.latitude().get().doubleValue());
    }
    if (geoLocation.longitude().isPresent()) {
      builder.setLongitude(geoLocation.longitude().get().doubleValue());
    }

    return builder.build();
  }

  @Override
  public GeoLocation apply(IaasEntities.GeoLocation geoLocation) {
    if (geoLocation == null) {
      return null;
    }
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
