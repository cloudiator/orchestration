package io.github.cloudiator.iaas.common.persistance.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.entities.CloudCredentialModel;
import javax.persistence.EntityManager;

public class CloudCredentialModelRepositoryJpa extends
    BaseModelRepositoryJpa<CloudCredentialModel> implements CloudCredentialModelRepository {

  @Inject
  protected CloudCredentialModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<CloudCredentialModel> type) {
    super(entityManager, type);
  }
}
