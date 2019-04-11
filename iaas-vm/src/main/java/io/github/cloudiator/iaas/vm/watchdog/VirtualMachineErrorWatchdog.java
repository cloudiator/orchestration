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

package io.github.cloudiator.iaas.vm.watchdog;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.domain.LocalVirtualMachineState;
import io.github.cloudiator.iaas.vm.state.VirtualMachineStateMachine;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineErrorWatchdog implements Schedulable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineErrorWatchdog.class);

  private final ComputeService computeService;
  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final VirtualMachineStateMachine virtualMachineStateMachine;

  @Inject
  public VirtualMachineErrorWatchdog(
      ComputeService computeService,
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      VirtualMachineStateMachine virtualMachineStateMachine) {
    this.computeService = computeService;
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.virtualMachineStateMachine = virtualMachineStateMachine;
  }

  @Override
  public long period() {
    return 60;
  }

  @Override
  public long delay() {
    return 0;
  }

  @Override
  public TimeUnit timeUnit() {
    return TimeUnit.SECONDS;
  }

  @Override
  public void run() {

    LOGGER.info(String.format("%s is running", this));

    final Iterable<VirtualMachine> virtualMachines = computeService.discoveryService()
        .listVirtualMachines();

    for (ExtendedVirtualMachine extendedVirtualMachine : virtualMachineDomainRepository.findAll()
        .stream().filter(
            extendedVirtualMachine -> extendedVirtualMachine.state()
                .equals(LocalVirtualMachineState.RUNNING)).collect(
            Collectors.toSet())) {

      LOGGER.debug(
          String.format("Checking if virtual machine %s still exists", extendedVirtualMachine));

      final boolean stillExists = checkIfStillExists(extendedVirtualMachine, virtualMachines);

      if (!stillExists) {
        LOGGER.warn(String.format("Virtual machine %s does not longer exist, marking failed",
            extendedVirtualMachine));

        virtualMachineStateMachine.fail(extendedVirtualMachine, new Object[0], null);
      } else {
        LOGGER.debug(String.format("Virtual machine %s still exists.", extendedVirtualMachine));
      }
    }

  }

  private boolean checkIfStillExists(ExtendedVirtualMachine extendedVirtualMachine,
      Iterable<VirtualMachine> virtualMachines) {

    for (VirtualMachine virtualMachine : virtualMachines) {
      if (virtualMachine.id().equals(extendedVirtualMachine.id())) {
        return true;
      }
    }

    return false;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
