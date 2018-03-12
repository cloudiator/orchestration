package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;

public class VirtualMachineDomainRepository {

  private final CloudDomainRepository cloudDomainRepository;
  private final ResourceRepository<HardwareModel> hardwareModelRepository;
  private final LocationModelRepository locationModelRepository;
  private final ResourceRepository<ImageModel> imageModelRepository;

  @Inject
  public VirtualMachineDomainRepository(
      CloudDomainRepository cloudDomainRepository,
      ResourceRepository<HardwareModel> hardwareModelRepository,
      LocationModelRepository locationModelRepository,
      ResourceRepository<ImageModel> imageModelRepository) {
    this.cloudDomainRepository = cloudDomainRepository;
    this.hardwareModelRepository = hardwareModelRepository;
    this.locationModelRepository = locationModelRepository;
    this.imageModelRepository = imageModelRepository;
  }

  public VirtualMachine findById(String id) {
    return null;
  }

  public VirtualMachine findByTenantAndId(String userId, String id) {
    return null;
  }

  public VirtualMachine findAll(String userId) {
    return null;
  }

  public VirtualMachine findAll() {
    return null;
  }

  public void save(VirtualMachine virtualMachine) {

    //retrieve or create the cloud




    //VirtualMachineModel virtualMachineModel = new VirtualMachineModel(virtualMachine.id(),
    //    virtualMachine.providerId(), virtualMachine.name());

  }


}
