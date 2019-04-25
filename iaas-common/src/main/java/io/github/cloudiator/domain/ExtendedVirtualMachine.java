/*
 * Copyright (c) 2014-2019 University of Ulm
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

package io.github.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.Stateful;
import java.util.Optional;
import java.util.Set;

public class ExtendedVirtualMachine implements VirtualMachine, Stateful<LocalVirtualMachineState> {

  private final VirtualMachine delegate;
  private final String userId;
  private LocalVirtualMachineState virtualMachineState;

  public ExtendedVirtualMachine(VirtualMachine delegate,
      String userId, LocalVirtualMachineState virtualMachineState) {

    checkNotNull(delegate, "delegate is null");
    checkNotNull(userId, "userId is null");
    checkNotNull(virtualMachineState, "virtualMachineState is null");

    this.delegate = delegate;
    this.userId = userId;
    this.virtualMachineState = virtualMachineState;
  }

  @Override
  public Set<IpAddress> ipAddresses() {
    return delegate.ipAddresses();
  }

  @Override
  public Set<IpAddress> publicIpAddresses() {
    return delegate.publicIpAddresses();
  }

  @Override
  public Set<IpAddress> privateIpAddresses() {
    return delegate.privateIpAddresses();
  }

  @Override
  public Optional<Image> image() {
    return delegate.image();
  }

  @Override
  public Optional<String> imageId() {
    return delegate.imageId();
  }

  @Override
  public Optional<HardwareFlavor> hardware() {
    return delegate.hardware();
  }

  @Override
  public Optional<String> hardwareId() {
    return delegate.hardwareId();
  }

  @Override
  public Optional<LoginCredential> loginCredential() {
    return delegate.loginCredential();
  }

  @Override
  public VirtualMachineState remoteState() {
    return delegate.remoteState();
  }

  @Override
  public String providerId() {
    return delegate.providerId();
  }

  @Override
  public String id() {
    return delegate.id();
  }

  @Override
  @JsonProperty
  public String name() {
    return delegate.name();
  }

  @Override
  public Optional<Location> location() {
    return delegate.location();
  }

  @Override
  public Optional<String> locationId() {
    return delegate.locationId();
  }


  public String getUserId() {
    return userId;
  }

  @Override
  public LocalVirtualMachineState state() {
    return virtualMachineState;
  }

  public void setState(LocalVirtualMachineState state) {
    this.virtualMachineState = state;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("delegate", delegate)
        .add("userId", userId)
        .add("virtualMachineState", virtualMachineState)
        .toString();
  }
}
