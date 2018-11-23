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

import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavorBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
class HardwareConverter implements OneWayConverter<HardwareModel, HardwareFlavor> {

  private final LocationConverter locationConverter = new LocationConverter();

  @Nullable
  @Override
  public HardwareFlavor apply(@Nullable HardwareModel hardwareModel) {
    if (hardwareModel == null) {
      return null;
    }
    return HardwareFlavorBuilder.newBuilder().name(hardwareModel.getName())
        .providerId(hardwareModel.getProviderId()).id(hardwareModel.getCloudUniqueId())
        .location(locationConverter.apply(hardwareModel.getLocationModel()))
        .mbRam(hardwareModel.hardwareOffer().getMbOfRam())
        .gbDisk(hardwareModel.hardwareOffer().getDiskSpace())
        .cores(hardwareModel.hardwareOffer().getNumberOfCores()).build();

  }
}
