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
