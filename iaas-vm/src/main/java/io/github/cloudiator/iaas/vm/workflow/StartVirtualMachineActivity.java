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

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 07.02.17.
 */
public class StartVirtualMachineActivity implements Activity {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(StartVirtualMachineActivity.class);
  private final ComputeService computeService;

  public StartVirtualMachineActivity(ComputeService computeService) {
    checkNotNull(computeService, "computeService is null");
    this.computeService = computeService;
  }

  @Override
  public Exchange execute(Exchange input) {
    VirtualMachineTemplate virtualMachineTemplate = input.getData(VirtualMachineTemplate.class)
        .orElseThrow(() -> new IllegalStateException(
            "Expected a VirtualMachineTemplate to be provided"));

    LOGGER.debug(String
        .format("Starting execution of StartVirtualMachineActivity %s using template %s.",
            this, virtualMachineTemplate));

    final VirtualMachine virtualMachine = computeService
        .createVirtualMachine(virtualMachineTemplate);

    LOGGER.debug(String
        .format("StartVirtualMachineActivity %s create virtual machine %s.",
            this, virtualMachine));

    return Exchange.of(virtualMachine);
  }
}
