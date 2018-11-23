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

package io.github.cloudiator.persistance;


import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class VirtualMachineDomainRepository {

  private final LoginCredentialDomainRepository loginCredentialDomainRepository;
  private final IpAddressDomainRepository ipAddressDomainRepository;
  private final VirtualMachineModelRepository virtualMachineModelRepository;
  private final VirtualMachineConverter virtualMachineConverter;
  private final TenantModelRepository tenantModelRepository;

  @Inject
  public VirtualMachineDomainRepository(
      LoginCredentialDomainRepository loginCredentialDomainRepository,
      IpAddressDomainRepository ipAddressDomainRepository,
      VirtualMachineModelRepository virtualMachineModelRepository,
      VirtualMachineConverter virtualMachineConverter,
      TenantModelRepository tenantModelRepository) {
    this.loginCredentialDomainRepository = loginCredentialDomainRepository;
    this.ipAddressDomainRepository = ipAddressDomainRepository;
    this.virtualMachineModelRepository = virtualMachineModelRepository;
    this.virtualMachineConverter = virtualMachineConverter;
    this.tenantModelRepository = tenantModelRepository;
  }

  public VirtualMachine findById(String id) {
    return virtualMachineConverter.apply(virtualMachineModelRepository.findByCloudUniqueId(id));
  }

  public VirtualMachine findByTenantAndId(String userId, String id) {
    return virtualMachineConverter
        .apply(virtualMachineModelRepository.findByCloudUniqueIdAndTenant(userId, id));
  }

  public List<VirtualMachine> findAll(String userId) {

    return virtualMachineModelRepository.findByTenant(userId).stream().map(virtualMachineConverter)
        .collect(Collectors
            .toList());
  }

  public List<VirtualMachine> findAll() {
    return virtualMachineModelRepository.findAll().stream().map(virtualMachineConverter)
        .collect(Collectors.toList());
  }

  public void save(VirtualMachine virtualMachine, String userId) {
    saveAndGet(virtualMachine, userId);
  }

  public void delete(String vmId, String userId) {
    VirtualMachineModel vm = virtualMachineModelRepository
        .findByCloudUniqueIdAndTenant(userId, vmId);
    checkState(vm != null, String.format("VM with id %s does not exist.", vmId));
    virtualMachineModelRepository.delete(vm);
  }

  VirtualMachineModel saveAndGet(VirtualMachine virtualMachine, String userId) {
    //retrieve an existing virtual machine
    VirtualMachineModel virtualMachineModel = virtualMachineModelRepository
        .findByCloudUniqueId(virtualMachine.id());
    if (virtualMachineModel == null) {
      virtualMachineModel = createModel(virtualMachine, userId);
    } else {
      virtualMachineModel = updateModel(virtualMachine, virtualMachineModel);
    }

    virtualMachineModelRepository.save(virtualMachineModel);

    return virtualMachineModel;
  }

  @Nullable
  private LoginCredentialModel createLoginCredentialModel(VirtualMachine domain) {
    if (domain.loginCredential().isPresent()) {
      return loginCredentialDomainRepository.saveAndGet(domain.loginCredential().get());
    }
    return null;
  }

  @Nullable
  private IpGroupModel createIpGroupModel(VirtualMachine domain) {
    return ipAddressDomainRepository.saveAndGet(domain.ipAddresses());
  }


  private VirtualMachineModel createModel(VirtualMachine virtualMachine, String userId) {

    //retrieve the cloud
    final String cloudId = IdScopedByClouds.from(virtualMachine.id()).cloudId();

    final TenantModel tenantModel = tenantModelRepository.createOrGet(userId);

    LoginCredentialModel loginCredentialModel = createLoginCredentialModel(virtualMachine);
    IpGroupModel ipGroupModel = createIpGroupModel(virtualMachine);

    return new VirtualMachineModel(virtualMachine.id(),
        virtualMachine.providerId(), virtualMachine.name(), cloudId, tenantModel,
        virtualMachine.locationId().orElse(null),
        loginCredentialModel, virtualMachine.imageId().orElse(null),
        virtualMachine.hardwareId().orElse(null), ipGroupModel);

  }

  private VirtualMachineModel updateModel(VirtualMachine virtualMachine,
      VirtualMachineModel virtualMachineModel) {
    //todo: Implement, currently noop
    return virtualMachineModel;
  }


}
