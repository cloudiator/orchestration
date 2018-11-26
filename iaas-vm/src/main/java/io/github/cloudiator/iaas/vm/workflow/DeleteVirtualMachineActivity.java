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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteVirtualMachineActivity implements Activity {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DeleteVirtualMachineActivity.class);

  private final ComputeService computeService;

  public DeleteVirtualMachineActivity(
      ComputeService computeService) {
    this.computeService = computeService;
  }

  @Override
  public Exchange execute(Exchange input) {
    String vmId = input.getData(String.class)
        .orElseThrow(() -> new IllegalStateException("Expected a virtual machine id."));

    VirtualMachine virtualMachine = computeService.discoveryService().getVirtualMachine(vmId);

    checkState(virtualMachine != null,
        String.format("Virtual machine with id %s does not exist.", vmId));

    LOGGER.info(
        String.format("%s is issuing delete request for virtual machine with id %s.", this, vmId));

    computeService.deleteVirtualMachine(vmId);

    LOGGER.info(String.format("%s has deleted virtual machine with id %s.", this, vmId));

    return Exchange.of(Void.class);

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
