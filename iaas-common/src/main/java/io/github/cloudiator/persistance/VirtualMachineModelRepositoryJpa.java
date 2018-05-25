package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;

class VirtualMachineModelRepositoryJpa extends
    BaseResourceRepositoryJpa<VirtualMachineModel> implements VirtualMachineModelRepository {

  @Inject
  public VirtualMachineModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<VirtualMachineModel> type) {
    super(entityManager, type);
  }
}
