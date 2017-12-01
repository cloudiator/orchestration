package io.github.cloudiator.iaas.common.persistance.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.iaas.common.persistance.entities.VirtualMachineModel;
import javax.persistence.EntityManager;

public class VirtualMachineModelRepositoryJpa extends
    BaseResourceRepositoryJpa<VirtualMachineModel> implements VirtualMachineModelRepository {

  @Inject
  public VirtualMachineModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<VirtualMachineModel> type) {
    super(entityManager, type);
  }
}
