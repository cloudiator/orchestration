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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.UnitOfWork;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import io.github.cloudiator.iaas.vm.EnrichVirtualMachine;
import io.github.cloudiator.iaas.vm.VirtualMachineStatistics;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import javax.persistence.EntityManager;
import org.cloudiator.messaging.MessageInterface;

@Singleton
public class VirtualMachineRequestWorkerFactory {

  private final UnitOfWork unitOfWork;
  private final Provider<EntityManager> entityManagerProvider;
  private final MessageInterface messageInterface;
  private final EnrichVirtualMachine enrichVirtualMachine;
  private final ComputeService computeService;
  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final VirtualMachineStatistics virtualMachineStatistics;

  @Inject
  public VirtualMachineRequestWorkerFactory(
      UnitOfWork unitOfWork,
      Provider<EntityManager> entityManagerProvider,
      MessageInterface messageInterface,
      EnrichVirtualMachine enrichVirtualMachine,
      ComputeService computeService,
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      VirtualMachineStatistics virtualMachineStatistics) {
    this.unitOfWork = unitOfWork;
    this.entityManagerProvider = entityManagerProvider;
    this.messageInterface = messageInterface;
    this.enrichVirtualMachine = enrichVirtualMachine;
    this.computeService = computeService;
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.virtualMachineStatistics = virtualMachineStatistics;
  }

  public VirtualMachineRequestWorker create(VirtualMachineRequest virtualMachineRequest) {
    return new VirtualMachineRequestWorker(virtualMachineRequest, unitOfWork, entityManagerProvider,
        messageInterface,
        enrichVirtualMachine, computeService, virtualMachineDomainRepository,
        virtualMachineStatistics);
  }

}
