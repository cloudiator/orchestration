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

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine.Builder;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class VirtualMachineMessageToVirtualMachine implements
    TwoWayConverter<IaasEntities.VirtualMachine, VirtualMachine> {

  public static final VirtualMachineMessageToVirtualMachine INSTANCE = new VirtualMachineMessageToVirtualMachine();

  private static final LoginCredentialMessageToLoginCredentialConverter LOGIN_CREDENTIAL_CONVERTER = LoginCredentialMessageToLoginCredentialConverter.INSTANCE;
  private ImageMessageToImageConverter imageConverter = ImageMessageToImageConverter.INSTANCE;
  private HardwareMessageToHardwareConverter hardwareConverter = HardwareMessageToHardwareConverter.INSTANCE;
  private LocationMessageToLocationConverter locationConverter = LocationMessageToLocationConverter.INSTANCE;
  private IpAddressMessageToIpAddress ipConverter = new IpAddressMessageToIpAddress();

  private VirtualMachineMessageToVirtualMachine() {
  }


  @Override
  public IaasEntities.VirtualMachine applyBack(VirtualMachine virtualMachine) {

    final Builder builder = IaasEntities.VirtualMachine.newBuilder()
        .setId(virtualMachine.id())
        .setProviderId(virtualMachine.providerId())
        .setName(virtualMachine.name());

    if (virtualMachine.location().isPresent()) {
      builder.setLocation(locationConverter.applyBack(virtualMachine.location().get()));
    }

    if (virtualMachine.image().isPresent()) {
      builder.setImage(imageConverter.applyBack(virtualMachine.image().get()));
    }

    if (virtualMachine.hardware().isPresent()) {
      builder.setHardware(hardwareConverter.applyBack(virtualMachine.hardware().get()));
    }

    if (virtualMachine.loginCredential().isPresent()) {
      builder.setLoginCredential(
          LOGIN_CREDENTIAL_CONVERTER.applyBack(virtualMachine.loginCredential().get()));
    }

    virtualMachine.ipAddresses().forEach(
        ipAddress -> builder.addIpAddresses(ipConverter.applyBack(ipAddress)));

    return builder.build();
  }

  @Override
  public VirtualMachine apply(IaasEntities.VirtualMachine virtualMachine) {

    VirtualMachineBuilder builder = VirtualMachineBuilder.newBuilder().id(virtualMachine.getId())
        .providerId(virtualMachine.getProviderId()).name(virtualMachine.getName());

    if (virtualMachine.hasImage()) {
      builder.image(imageConverter.apply(virtualMachine.getImage()));
    }

    if (virtualMachine.hasLocation()) {
      builder.location(locationConverter.apply(virtualMachine.getLocation()));
    }

    if (virtualMachine.hasHardware()) {
      builder.hardware(hardwareConverter.apply(virtualMachine.getHardware()));
    }

    if (virtualMachine.hasLoginCredential()) {
      builder.loginCredential(LOGIN_CREDENTIAL_CONVERTER.apply(virtualMachine.getLoginCredential()));
    }

    virtualMachine.getIpAddressesList().forEach(
        ipAddress -> builder.addIpAddress(ipConverter.apply(ipAddress)));

    return builder.build();

  }
}
