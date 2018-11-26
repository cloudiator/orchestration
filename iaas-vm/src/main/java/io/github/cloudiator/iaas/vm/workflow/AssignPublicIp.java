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

package io.github.cloudiator.iaas.vm.workflow;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;

/**
 * Created by daniel on 07.02.17.
 */
public class AssignPublicIp implements Activity {

  private final static String NO_PUBLIC_IP = "Virtual machine %s has no public ip and public ip service is not supported for compute service %s.";
  private final ComputeService computeService;

  public AssignPublicIp(ComputeService computeService) {
    checkNotNull(computeService, "computeService is null");
    this.computeService = computeService;
  }

  @Override
  public Exchange execute(Exchange input) {

    final VirtualMachine virtualMachine = input.getData(VirtualMachine.class).orElseThrow(
        () -> new IllegalStateException("Expected a virtual machine to be provided."));

    if (!virtualMachine.publicIpAddresses().isEmpty()) {
      return input;
    }

    checkState(computeService.publicIpExtension().isPresent(),
        String.format(NO_PUBLIC_IP, virtualMachine, computeService));

    final String publicIp = computeService.publicIpExtension().get()
        .addPublicIp(virtualMachine.id());

    return Exchange.of(VirtualMachineBuilder.of(virtualMachine).addIpString(publicIp).build());
  }
}
