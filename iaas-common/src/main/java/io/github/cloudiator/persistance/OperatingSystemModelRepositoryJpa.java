package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;

public class OperatingSystemModelRepositoryJpa extends
    BaseModelRepositoryJpa<OperatingSystemModel> implements OperatingSystemModelRepository {

  @Inject
  protected OperatingSystemModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<OperatingSystemModel> type) {
    super(entityManager, type);
  }
}
