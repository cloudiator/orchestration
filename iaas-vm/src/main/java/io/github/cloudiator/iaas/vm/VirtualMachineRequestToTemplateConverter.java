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
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplateBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;

public class VirtualMachineRequestToTemplateConverter implements
    OneWayConverter<VirtualMachineRequest, VirtualMachineTemplate> {

  @Override
  public VirtualMachineTemplate apply(VirtualMachineRequest virtualMachineRequest) {

    if (virtualMachineRequest == null) {
      return null;
    }

    final VirtualMachineTemplateBuilder templateBuilder = VirtualMachineTemplateBuilder.newBuilder()
        .hardwareFlavor(virtualMachineRequest.getHardware())
        .image(virtualMachineRequest.getImage()).location(virtualMachineRequest.getLocation())
        .name(virtualMachineRequest.getName());
    return templateBuilder.build();
  }
}
