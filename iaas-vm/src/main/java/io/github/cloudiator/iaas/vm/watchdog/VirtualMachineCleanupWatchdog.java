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
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineCleanupWatchdog implements Schedulable {

  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final ComputeService computeService;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineCleanupWatchdog.class);

  private static final Map<VirtualMachine, Long> MARKED_FOR_DELETION = new HashMap<>();

  private static final long TIMEOUT = 600000;


  @Inject
  public VirtualMachineCleanupWatchdog(
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      ComputeService computeService) {
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.computeService = computeService;
  }

  @Transactional
  protected List<ExtendedVirtualMachine> localVms() {
    return virtualMachineDomainRepository.findAll();
  }

  @Override
  public long period() {
    return 4;
  }

  @Override
  public long delay() {
    return 0;
  }

  @Override
  public TimeUnit timeUnit() {
    return TimeUnit.MINUTES;
  }

  @Override
  public void run() {

    LOGGER.info(String.format("%s is starting execution.", this));

    final Iterable<VirtualMachine> remoteVMs = computeService.discoveryService()
        .listVirtualMachines();

    final List<ExtendedVirtualMachine> localVMs = localVms();

    Set<VirtualMachine> remoteOnly = new HashSet<>();
    //compare
    for (VirtualMachine remoteVM : remoteVMs) {
      remoteOnly.add(remoteVM);
      for (ExtendedVirtualMachine localVM : localVMs) {
        if (localVM.id().equals(remoteVM.id())) {
          remoteOnly.remove(remoteVM);
        }
      }
    }

    //mark remote only vms for deletion
    for (VirtualMachine virtualMachine : remoteOnly) {
      if (!MARKED_FOR_DELETION.containsKey(virtualMachine)) {
        LOGGER.info(
            String.format("Marking virtual machine %s for deletion as it only exist remotely",
                virtualMachine));
        MARKED_FOR_DELETION.put(virtualMachine, System.currentTimeMillis());
      }
    }

    //remove no longer existing remote only vms
    MARKED_FOR_DELETION.entrySet().removeIf(entry -> {

      boolean notLongerOrphaned = !remoteOnly.contains(entry.getKey());

      if (notLongerOrphaned) {
        LOGGER.info(
            String.format("Virtual machine %s is no longer marked for deletion.", entry.getKey()));
      }

      return notLongerOrphaned;

    });

    //trigger cleanup
    MARKED_FOR_DELETION.entrySet().removeIf(entry -> {
      boolean toCleanup = System.currentTimeMillis() - entry.getValue() >= TIMEOUT;

      if (toCleanup) {
        LOGGER.info(String.format(
            "Virtual machine %s is orphaned for more than %s milliseconds. Triggering cleanup",
            entry.getKey(), TIMEOUT));
        cleanup(entry.getKey());
      }

      return toCleanup;
    });

  }

  private void cleanup(VirtualMachine remote) {

    LOGGER.debug(
        String.format("Virtual machine %s was marked as obsolete, starting cleanup.", remote));

    try {
      computeService.deleteVirtualMachine(remote.id());
    } catch (Exception e) {
      LOGGER.warn(String.format("Failure during clean up of virtual machine %s. Ignoring.", remote),
          e);
    }

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
