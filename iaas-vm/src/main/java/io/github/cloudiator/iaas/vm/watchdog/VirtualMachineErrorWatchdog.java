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
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByCloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.domain.LocalVirtualMachineState;
import io.github.cloudiator.iaas.vm.state.VirtualMachineStateMachine;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineErrorWatchdog implements Schedulable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineErrorWatchdog.class);

  private final ComputeService computeService;
  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final VirtualMachineStateMachine virtualMachineStateMachine;
  private final CloudRegistry cloudRegistry;

  private static Set<String> FAILED_IN_LAST_ITERATION = new HashSet<>();

  @Inject
  public VirtualMachineErrorWatchdog(
      ComputeService computeService,
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      VirtualMachineStateMachine virtualMachineStateMachine,
      CloudRegistry cloudRegistry) {
    this.computeService = computeService;
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.virtualMachineStateMachine = virtualMachineStateMachine;
    this.cloudRegistry = cloudRegistry;
  }

  @Override
  public long period() {
    return 3;
  }

  @Override
  public long delay() {
    return 0;
  }

  @Override
  public TimeUnit timeUnit() {
    return TimeUnit.MINUTES;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  List<ExtendedVirtualMachine> getVms() {
    return virtualMachineDomainRepository.findAll();
  }

  @Override
  public void run() {

    try {

      LOGGER.info(String.format("%s is running", this));

      final Iterable<VirtualMachine> remoteVirtualMachines = computeService.discoveryService()
          .listVirtualMachines();

      LOGGER.debug("Remotely existing virtual machines: " + StreamSupport
          .stream(remoteVirtualMachines.spliterator(), false).map(Identifiable::id)
          .collect(Collectors.toSet()));

      for (ExtendedVirtualMachine localVM : getVms()
          .stream().filter(
              extendedVirtualMachine -> extendedVirtualMachine.state()
                  .equals(LocalVirtualMachineState.RUNNING)).collect(
              Collectors.toSet())) {

        //skip the still exists check if the corresponding cloud is in ERROR state
        //as this will cause the machine to disappear.
        final IdScopedByCloud idScopedByCloud = IdScopedByClouds.from(localVM.id());

        if (!cloudRegistry.isRegistered(idScopedByCloud.cloudId())) {
          LOGGER.warn(
              String.format("Skipping vm %s as corresponding cloud is in error state.", localVM));
          continue;
        }

        LOGGER.debug(
            String.format("Checking if virtual machine with id %s still exists",
                localVM.id()));

        final boolean stillExists = checkIfStillExists(localVM, remoteVirtualMachines);

        if (!stillExists) {
          if (FAILED_IN_LAST_ITERATION.contains(localVM.id())) {
            LOGGER
                .warn(
                    String.format("Virtual machine %s does not longer exist, marking it as failed.",
                        localVM));
            FAILED_IN_LAST_ITERATION.remove(localVM.id());
            virtualMachineStateMachine.fail(localVM, new Object[0], null);
          } else {
            LOGGER.warn(String.format(
                "Virtual machine %s failed for the first time. Checking again at the next iteration.",
                localVM));
            FAILED_IN_LAST_ITERATION.add(localVM.id());
          }
        } else {
          LOGGER.debug(
              String.format("Virtual machine with id %s still exists.", localVM.id()));
          FAILED_IN_LAST_ITERATION.remove(localVM.id());
        }
      }
    } catch (Exception e) {
      LOGGER.error(String.format(
          "%s encountered an unexpected exception: %s. Caught to allow further execution.",
          this, e.getMessage()), e);
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
