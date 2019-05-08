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

package io.github.cloudiator.iaas.vm;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.messaging.HardwareMessageRepository;
import io.github.cloudiator.messaging.ImageMessageRepository;
import io.github.cloudiator.messaging.LocationMessageRepository;

public class EnrichVirtualMachine {

  private final HardwareMessageRepository hardwareMessageRepository;
  private final LocationMessageRepository locationMessageRepository;
  private final ImageMessageRepository imageMessageRepository;

  @Inject
  public EnrichVirtualMachine(
      HardwareMessageRepository hardwareMessageRepository,
      LocationMessageRepository locationMessageRepository,
      ImageMessageRepository imageMessageRepository) {
    this.hardwareMessageRepository = hardwareMessageRepository;
    this.locationMessageRepository = locationMessageRepository;
    this.imageMessageRepository = imageMessageRepository;
  }

  public ExtendedVirtualMachine update(String userId, VirtualMachineTemplate virtualMachineTemplate,
      ExtendedVirtualMachine virtualMachine) {

    String hardwareId = virtualMachineTemplate.hardwareFlavorId();
    if (virtualMachine.hardware().isPresent()) {
      hardwareId = virtualMachine.hardware().get().id();
    }

    String imageId = virtualMachineTemplate.imageId();
    if (virtualMachine.image().isPresent()) {
      imageId = virtualMachine.image().get().id();
    }

    String locationId = virtualMachineTemplate.locationId();
    if (virtualMachine.location().isPresent()) {
      locationId = virtualMachine.location().get().id();
    }

    final HardwareFlavor hardware = hardwareMessageRepository.getById(userId, hardwareId);
    final Image image = imageMessageRepository.getById(userId, imageId);
    final Location location = locationMessageRepository.getById(userId, locationId);

    return new ExtendedVirtualMachine(
        VirtualMachineBuilder.of(virtualMachine).hardware(hardware).image(image)
            .location(location).build(), virtualMachine.getUserId(), virtualMachine.state());
  }

}
