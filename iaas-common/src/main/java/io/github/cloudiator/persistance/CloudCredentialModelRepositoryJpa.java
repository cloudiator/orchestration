package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
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
