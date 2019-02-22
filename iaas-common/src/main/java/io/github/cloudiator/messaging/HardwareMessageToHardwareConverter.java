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

import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavorBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.DiscoveredHardware;
import io.github.cloudiator.domain.DiscoveredLocation;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.HardwareFlavor.Builder;

/**
 * Created by daniel on 09.06.17.
 */
public class HardwareMessageToHardwareConverter implements
    TwoWayConverter<IaasEntities.HardwareFlavor, DiscoveredHardware> {

  public static final HardwareMessageToHardwareConverter INSTANCE = new HardwareMessageToHardwareConverter();

  private static final LocationMessageToLocationConverter LOCATION_CONVERTER = LocationMessageToLocationConverter.INSTANCE;
  private static final DiscoveryItemStateConverter DISCOVERY_ITEM_STATE_CONVERTER = DiscoveryItemStateConverter.INSTANCE;

  private HardwareMessageToHardwareConverter() {
  }

  @Override
  public IaasEntities.HardwareFlavor applyBack(DiscoveredHardware hardwareFlavor) {
    Builder builder = IaasEntities.HardwareFlavor.newBuilder()
        .setCores(hardwareFlavor.numberOfCores())
        .setId(hardwareFlavor.id()).setProviderId(hardwareFlavor.providerId())
        .setRam(hardwareFlavor.mbRam()).setName(hardwareFlavor.name())
        .setState(DISCOVERY_ITEM_STATE_CONVERTER.applyBack(hardwareFlavor.state()))
        .setUserId(hardwareFlavor.userId());
    if (hardwareFlavor.gbDisk().isPresent()) {
      builder.setDisk(hardwareFlavor.gbDisk().get());
    }
    if (hardwareFlavor.location().isPresent()) {
      builder.setLocation(LOCATION_CONVERTER.applyBack(
          (DiscoveredLocation) hardwareFlavor.location().get()));
    }
    return builder.build();
  }

  @Override
  public DiscoveredHardware apply(IaasEntities.HardwareFlavor hardwareFlavor) {

    final HardwareFlavorBuilder hardwareFlavorBuilder = HardwareFlavorBuilder.newBuilder()
        .cores(hardwareFlavor.getCores())
        .id(hardwareFlavor.getId()).name(hardwareFlavor.getName())
        .providerId(hardwareFlavor.getProviderId()).mbRam(hardwareFlavor.getRam());

    if (hardwareFlavor.hasLocation()) {
      hardwareFlavorBuilder.location(LOCATION_CONVERTER.apply(hardwareFlavor.getLocation()));
    }

    if (hardwareFlavor.getDisk() != 0) {
      hardwareFlavorBuilder.gbDisk(hardwareFlavor.getDisk());
    }

    return new DiscoveredHardware(hardwareFlavorBuilder.build(),
        DISCOVERY_ITEM_STATE_CONVERTER.apply(hardwareFlavor.getState()),
        hardwareFlavor.getUserId());
  }
}
