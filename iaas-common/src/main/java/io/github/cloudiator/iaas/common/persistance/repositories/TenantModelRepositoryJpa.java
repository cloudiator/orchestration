package io.github.cloudiator.iaas.common.persistance.repositories;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.entities.Tenant;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 31.05.17.
 */
public class TenantModelRepositoryJpa extends BaseModelRepositoryJpa<Tenant> implements
    TenantModelRepository {

  @Inject
  TenantModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<Tenant> type) {
    super(entityManager, type);
  }

  @Override
  public Tenant findByUserId(String userId) {
    checkNotNull(userId, "userId is null");
    String queryString = String
        .format("from %s where userId=:userId", type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId);
    try {
      //noinspection unchecked
      return (Tenant) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public Tenant createOrGet(String userId) {
    checkNotNull(userId, "userId is null");
    final Tenant byUserId = findByUserId(userId);
    if (byUserId != null) {
      return byUserId;
    }
    Tenant tenant = new Tenant(userId);
    save(tenant);
    return tenant;
  }
}
