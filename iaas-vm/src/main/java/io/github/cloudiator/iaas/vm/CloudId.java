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

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByCloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import java.util.function.Function;

public class CloudId implements Function<VirtualMachineTemplate, String> {

  public static CloudId instance() {
    return new CloudId();
  }

  @Override
  public String apply(VirtualMachineTemplate virtualMachineTemplate) {
    IdScopedByCloud image = IdScopedByClouds.from(virtualMachineTemplate.imageId());
    IdScopedByCloud location = IdScopedByClouds.from(virtualMachineTemplate.locationId());
    IdScopedByCloud hardware = IdScopedByClouds.from(virtualMachineTemplate.hardwareFlavorId());

    if (image.cloudId().equals(location.cloudId()) && location.cloudId()
        .equals(hardware.cloudId())) {
      return image.cloudId();
    }
    throw new IllegalStateException(String
        .format("CloudIds are not equal. Image: %s, Location %s, Hardware %s.", image.cloudId(),
            location.cloudId(), hardware.cloudId()));

  }
}
