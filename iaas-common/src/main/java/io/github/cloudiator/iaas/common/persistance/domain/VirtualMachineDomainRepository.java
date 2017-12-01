package io.github.cloudiator.iaas.common.persistance.domain;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareModel;
import io.github.cloudiator.iaas.common.persistance.entities.ImageModel;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.ResourceRepository;

public class VirtualMachineDomainRepository {

  private final ResourceRepository<HardwareModel> hardwareModelRepository;
  private final LocationModelRepository locationModelRepository;
  private final ResourceRepository<ImageModel> imageModelRepository;

  public VirtualMachineDomainRepository(
      ResourceRepository<HardwareModel> hardwareModelRepository,
      LocationModelRepository locationModelRepository,
      ResourceRepository<ImageModel> imageModelRepository) {
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

  }


}
