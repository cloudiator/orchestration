/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 21.06.15.
 */
class BaseResourceRepositoryJpa<T extends ResourceModel>
    extends BaseModelRepositoryJpa<T> implements ResourceRepository<T> {

  @Inject
  public BaseResourceRepositoryJpa(Provider<EntityManager> entityManager, TypeLiteral<T> type) {
    super(entityManager, type);
  }

  @Nullable
  @Override
  public T findByCloudUniqueId(String cloudUniqueId) {
    checkNotNull(cloudUniqueId, "cloudUniqueId is null");
    String queryString = String
        .format("from %s where cloudUniqueId=:cloudUniqueId", type.getName());
    Query query = em().createQuery(queryString).setParameter("cloudUniqueId", cloudUniqueId);
    try {
      //noinspection unchecked
      return (T) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<T> findByTenant(String tenant) {
    checkNotNull(tenant, "tenant is null");
    String queryString = String.format(
        "select resource from %s resource inner join resource.cloudModel cloud inner join cloud.tenantModel ct where ct.userId=:tenant",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant);
    //noinspection unchecked
    return (List<T>) query.getResultList();
  }

  @Override
  public T findByCloudUniqueIdAndTenant(String tenant, String cloudUniqueId) {
    checkNotNull(tenant, "tenant is null");
    checkNotNull(cloudUniqueId, "id is null");
    String queryString = String.format(
        "select resource from %s resource inner join resource.cloudModel cloud inner join cloud.tenantModel ct where ct.userId=:tenant and resource.cloudUniqueId = :id",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant)
        .setParameter("id", cloudUniqueId);
    //noinspection unchecked
    try {
      //noinspection unchecked
      return (T) query.getSingleResult();
    } catch (NoResultException ignored) {
      return null;
    }
  }

  @Override
  public List<T> findByTenantAndCloud(String tenant, String cloudId) {
    checkNotNull(tenant, "tenant is null");
    checkNotNull(cloudId, "cloudId is null");
    String queryString = String.format(
        "select resource from %s resource inner join resource.cloudModel cloud inner join cloud.tenantModel ct where ct.userId=:tenant and cloud.cloudId = :cloudId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant)
        .setParameter("cloudId", cloudId);
    //noinspection unchecked
    return (List<T>) query.getResultList();
  }
}
