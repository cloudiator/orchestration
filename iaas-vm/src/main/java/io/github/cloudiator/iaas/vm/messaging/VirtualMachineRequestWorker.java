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

package io.github.cloudiator.iaas.vm.messaging;

import static com.google.common.base.Preconditions.checkState;
import static jersey.repackaged.com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.domain.LocalVirtualMachineState;
import io.github.cloudiator.iaas.vm.EnrichVirtualMachine;
import io.github.cloudiator.iaas.vm.VirtualMachineRequestToTemplateConverter;
import io.github.cloudiator.iaas.vm.VirtualMachineStatistics;
import io.github.cloudiator.iaas.vm.workflow.CreateVirtualMachineWorkflow;
import io.github.cloudiator.iaas.vm.workflow.Exchange;
import io.github.cloudiator.messaging.VirtualMachineMessageToVirtualMachine;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineRequestWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineRequestWorker.class);

  private final VirtualMachineRequest virtualMachineRequest;

  private final MessageInterface messageInterface;
  private final EnrichVirtualMachine enrichVirtualMachine;
  private final ComputeService computeService;
  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final VirtualMachineStatistics virtualMachineStatistics;

  private static final VirtualMachineRequestToTemplateConverter VIRTUAL_MACHINE_REQUEST_TO_TEMPLATE_CONVERTER = new VirtualMachineRequestToTemplateConverter();
  private static final VirtualMachineMessageToVirtualMachine VM_CONVERTER = VirtualMachineMessageToVirtualMachine.INSTANCE;

  @Inject
  VirtualMachineRequestWorker(
      @Assisted VirtualMachineRequest virtualMachineRequest,
      MessageInterface messageInterface,
      EnrichVirtualMachine enrichVirtualMachine,
      ComputeService computeService,
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      VirtualMachineStatistics virtualMachineStatistics) {
    this.virtualMachineRequest = virtualMachineRequest;
    this.messageInterface = messageInterface;
    this.enrichVirtualMachine = enrichVirtualMachine;
    this.computeService = computeService;
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.virtualMachineStatistics = virtualMachineStatistics;
  }

  private void doWork(VirtualMachineRequest virtualMachineRequest) {
    LOGGER.debug(String.format("Starting execution of new virtual machine request %s.",
        virtualMachineRequest));

    try {

      final String userId = virtualMachineRequest.getCreateVirtualMachineRequestMessage()
          .getUserId();

      checkNotNull(virtualMachineRequest, "userCreateVirtualMachineRequest is null");

      VirtualMachineTemplate virtualMachineTemplate = VIRTUAL_MACHINE_REQUEST_TO_TEMPLATE_CONVERTER
          .apply(virtualMachineRequest.getCreateVirtualMachineRequestMessage()
              .getVirtualMachineRequest());

      LOGGER.debug(String.format("Using virtual machine template %s to start virtual machine.",
          virtualMachineTemplate));

      CreateVirtualMachineWorkflow createVirtualMachineWorkflow = new CreateVirtualMachineWorkflow(
          computeService);

      LOGGER.debug("Starting execution of workflow for virtual machine.");

      final long startTime = System.currentTimeMillis();

      Exchange result = createVirtualMachineWorkflow
          .execute(Exchange.of(virtualMachineTemplate));

      checkState(result.getData(VirtualMachine.class).isPresent(),
          "No virtual machine created by workflow.");

      ExtendedVirtualMachine virtualMachine = new ExtendedVirtualMachine(
          result.getData(VirtualMachine.class).get(), userId, LocalVirtualMachineState.RUNNING);

      //decorate virtual machine
      final ExtendedVirtualMachine update = enrichVirtualMachine
          .update(userId,
              virtualMachineTemplate,
              virtualMachine);

      final long stopTime = System.currentTimeMillis();

      virtualMachineStatistics
          .virtualMachineStartTime(userId, virtualMachine,
              stopTime - startTime);

      //persist the vm
      persistVirtualMachine(update, userId);

      messageInterface.reply(virtualMachineRequest.getId(),
          VirtualMachineCreatedResponse.newBuilder()
              .setVirtualMachine(VM_CONVERTER.applyBack(update)).build());

    } catch (Exception e) {
      LOGGER.error("Error during execution of virtual machine creation", e);
      messageInterface
          .reply(VirtualMachineCreatedResponse.class,
              virtualMachineRequest.getId(),
              Error.newBuilder().setCode(500)
                  .setMessage("Error during creation of virtual machine: " + e.getMessage())
                  .build());
    }
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void persistVirtualMachine(ExtendedVirtualMachine vm, String userId) {
    virtualMachineDomainRepository.save(vm, userId);
  }

  @Override
  public void run() {
    doWork(virtualMachineRequest);
  }
}
