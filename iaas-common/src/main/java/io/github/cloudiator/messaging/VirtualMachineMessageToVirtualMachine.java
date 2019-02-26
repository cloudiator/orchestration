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

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.DiscoveredHardware;
import io.github.cloudiator.domain.DiscoveredImage;
import io.github.cloudiator.domain.DiscoveredLocation;
import io.github.cloudiator.domain.DiscoveryItemState;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.domain.LocalVirtualMachineState;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine.Builder;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class VirtualMachineMessageToVirtualMachine implements
    TwoWayConverter<IaasEntities.VirtualMachine, ExtendedVirtualMachine> {

  public static final VirtualMachineMessageToVirtualMachine INSTANCE = new VirtualMachineMessageToVirtualMachine();

  private static final LoginCredentialMessageToLoginCredentialConverter LOGIN_CREDENTIAL_CONVERTER = LoginCredentialMessageToLoginCredentialConverter.INSTANCE;
  private ImageMessageToImageConverter imageConverter = ImageMessageToImageConverter.INSTANCE;
  private HardwareMessageToHardwareConverter hardwareConverter = HardwareMessageToHardwareConverter.INSTANCE;
  private LocationMessageToLocationConverter locationConverter = LocationMessageToLocationConverter.INSTANCE;
  private IpAddressMessageToIpAddress ipConverter = new IpAddressMessageToIpAddress();
  public static final VirtualMachineStateConverter VM_STATE_CONVERTER = new VirtualMachineStateConverter();

  private VirtualMachineMessageToVirtualMachine() {
  }


  @Override
  public IaasEntities.VirtualMachine applyBack(ExtendedVirtualMachine virtualMachine) {

    final Builder builder = IaasEntities.VirtualMachine.newBuilder()
        .setId(virtualMachine.id())
        .setProviderId(virtualMachine.providerId())
        .setName(virtualMachine.name())
        .setUserId(virtualMachine.getUserId())
        .setState(VM_STATE_CONVERTER.apply(virtualMachine.state()));

    if (virtualMachine.location().isPresent()) {
      builder.setLocation(
          locationConverter.applyBack(new DiscoveredLocation(virtualMachine.location().get(),
              DiscoveryItemState.UNKNOWN, virtualMachine.getUserId())));
    }

    if (virtualMachine.image().isPresent()) {
      builder.setImage(imageConverter.applyBack(
          new DiscoveredImage(virtualMachine.image().get(), DiscoveryItemState.UNKNOWN,
              virtualMachine.getUserId())));
    }

    if (virtualMachine.hardware().isPresent()) {
      builder.setHardware(hardwareConverter.applyBack(
          new DiscoveredHardware(virtualMachine.hardware().get(), DiscoveryItemState.UNKNOWN,
              virtualMachine.getUserId())));
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
  public ExtendedVirtualMachine apply(IaasEntities.VirtualMachine virtualMachine) {

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
      builder
          .loginCredential(LOGIN_CREDENTIAL_CONVERTER.apply(virtualMachine.getLoginCredential()));
    }

    virtualMachine.getIpAddressesList().forEach(
        ipAddress -> builder.addIpAddress(ipConverter.apply(ipAddress)));

    return new ExtendedVirtualMachine(builder.build(), virtualMachine.getUserId(),
        VM_STATE_CONVERTER.applyBack(virtualMachine.getState()));

  }

  public static class VirtualMachineStateConverter implements
      TwoWayConverter<LocalVirtualMachineState, IaasEntities.VirtualMachineState> {

    private VirtualMachineStateConverter() {
    }

    @Override
    public LocalVirtualMachineState applyBack(IaasEntities.VirtualMachineState vmState) {
      switch (vmState) {
        case VM_STATE_RUNNING:
          return LocalVirtualMachineState.RUNNING;
        case VM_STATE_ERROR:
          return LocalVirtualMachineState.ERROR;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown vmState " + vmState);
      }
    }

    @Override
    public IaasEntities.VirtualMachineState apply(LocalVirtualMachineState vmState) {

      switch (vmState) {
        case ERROR:
          return IaasEntities.VirtualMachineState.VM_STATE_ERROR;
        case RUNNING:
          return IaasEntities.VirtualMachineState.VM_STATE_RUNNING;
        default:
          throw new AssertionError("Unknown vm state " + vmState);
      }
    }
  }
}
