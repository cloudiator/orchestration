package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;

public class CloudConfigurationModelRepositoryJpa extends
    BaseModelRepositoryJpa<CloudConfigurationModel> implements CloudConfigurationModelRepository {

  @Inject
  protected CloudConfigurationModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<CloudConfigurationModel> type) {
    super(entityManager, type);
  }
}
