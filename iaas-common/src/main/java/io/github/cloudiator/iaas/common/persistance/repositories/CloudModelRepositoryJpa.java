package io.github.cloudiator.iaas.common.persistance.repositories;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudModelRepositoryJpa extends BaseModelRepositoryJpa<CloudModel> implements
    CloudModelRepository {

  @Inject
  CloudModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<CloudModel> type) {
    super(entityManager, type);
  }

  @Override
  @Nullable
  public CloudModel getByCloudId(String cloudId) {
    checkNotNull(cloudId, "cloudId is null");
    String queryString = String
        .format("from %s where cloudId=:cloudId", type.getName());
    Query query = em().createQuery(queryString).setParameter("cloudId", cloudId);
    try {
      //noinspection unchecked
      return (CloudModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<CloudModel> getByTenant(String userId) {
    checkNotNull(userId, "userId is null");
    String queryString = String.format(
        "select cloud from %s cloud inner join cloud.tenantModel tenant where tenant.userId = :userId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId);
    return query.getResultList();
  }

  @Override
  @Nullable
  public CloudModel getByTenantAndId(String userId, String cloudId) {
    checkNotNull(userId, "userId is null");
    checkNotNull(cloudId, "cloudId is null");
    String queryString = String.format(
        "select cloud from %s cloud inner join cloud.tenantModel tenant where tenant.userId = :userId and cloud.cloudId = :cloudId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId)
        .setParameter("cloudId", cloudId);
    @SuppressWarnings("unchecked") List<CloudModel> clouds = query.getResultList();
    return clouds.stream().findFirst().orElse(null);
  }
}
