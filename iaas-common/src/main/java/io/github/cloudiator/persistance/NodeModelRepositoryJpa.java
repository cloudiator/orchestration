package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

class NodeModelRepositoryJpa extends BaseModelRepositoryJpa<NodeModel> implements
    NodeModelRepository {

  @Inject
  protected NodeModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<NodeModel> type) {
    super(entityManager, type);
  }

  @Override
  public List<NodeModel> getByTenant(String userId) {
    return null;
  }

  @Nullable
  @Override
  public NodeModel getByTenantAndId(String userId, String cloudId) {
    return null;
  }
}
