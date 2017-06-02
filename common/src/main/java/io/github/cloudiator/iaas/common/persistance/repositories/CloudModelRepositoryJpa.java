package io.github.cloudiator.iaas.common.persistance.repositories;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.iaas.common.persistance.entities.Cloud;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudModelRepositoryJpa extends BaseModelRepositoryJpa<Cloud> implements
    CloudModelRepository {

  @Inject
  CloudModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<Cloud> type) {
    super(entityManager, type);
  }

  @Override
  public io.github.cloudiator.iaas.common.persistance.entities.Cloud getByCloudId(String cloudId) {
    checkNotNull(cloudId, "cloudId is null");
    String queryString = String
        .format("from %s where cloudId=:cloudId", type.getName());
    Query query = em().createQuery(queryString).setParameter("cloudId", cloudId);
    try {
      //noinspection unchecked
      return (Cloud) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}
