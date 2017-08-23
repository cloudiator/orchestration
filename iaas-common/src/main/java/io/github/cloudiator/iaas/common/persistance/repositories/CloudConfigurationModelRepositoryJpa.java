package io.github.cloudiator.iaas.common.persistance.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.entities.CloudConfigurationModel;
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
