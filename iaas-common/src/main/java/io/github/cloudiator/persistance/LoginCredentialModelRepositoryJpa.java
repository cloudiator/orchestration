package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;

public class LoginCredentialModelRepositoryJpa extends
    BaseModelRepositoryJpa<LoginCredentialModel> implements LoginCredentialModelRepository {

  @Inject
  protected LoginCredentialModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<LoginCredentialModel> type) {
    super(entityManager, type);
  }
}
