package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;

class IpGroupModelRepositoryJpa extends BaseModelRepositoryJpa<IpGroupModel> implements
    IpGroupModelRepository {

  @Inject
  protected IpGroupModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<IpGroupModel> type) {
    super(entityManager, type);
  }
}
