package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class NodeGroupModelRepositoryJpa extends BaseModelRepositoryJpa<NodeGroupModel> implements
    NodeGroupModelRepository {

  @Inject
  protected NodeGroupModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<NodeGroupModel> type) {
    super(entityManager, type);
  }

  @Override
  public NodeGroupModel findByTenantAndDomainId(String userId, String domainId) {
    checkNotNull(userId, "userId is null");
    checkNotNull(domainId, "domainId is null");
    String queryString = String.format(
        "select nodeGroup from %s nodeGroup inner join nodeGroup.tenantModel tenant where tenant.userId = :userId and nodeGroup.domainId = :domainId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId)
        .setParameter("domainId", domainId);
    @SuppressWarnings("unchecked") List<NodeGroupModel> nodeGroups = query.getResultList();
    return nodeGroups.stream().findFirst().orElse(null);
  }

  @Override
  public List<NodeGroupModel> findByTenant(String userId) {
    checkNotNull(userId, "userId is null");
    String queryString = String
        .format(
            "from %s nodeGroup inner join nodeGroup.tenantModel tenant where tenant.userId = :userId",
            type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId);
    //noinspection unchecked
    return query.getResultList();
  }
}
