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
class LocationModelRepositoryJpa
    extends BaseModelRepositoryJpa<LocationModel> implements LocationModelRepository {


  @Inject
  LocationModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<LocationModel> type) {
    super(entityManager, type);
  }

  @Nullable
  @Override
  public LocationModel findByCloudUniqueId(String cloudUniqueId) {
    checkNotNull(cloudUniqueId, "cloudUniqueId is null");
    String queryString = String
        .format("from %s where cloudUniqueId=:cloudUniqueId", type.getName());
    Query query = em().createQuery(queryString).setParameter("cloudUniqueId", cloudUniqueId);
    try {
      //noinspection unchecked
      return (LocationModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<LocationModel> findByTenant(String tenant) {
    checkNotNull(tenant, "tenant is null");
    String queryString = String.format(
        "select location from %s location inner join location.cloudModel cloud inner join cloud.tenantModel tenant where tenant.userId = :tenant",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant);
    //noinspection unchecked
    return (List<LocationModel>) query.getResultList();
  }

  @Override
  public LocationModel findByCloudUniqueIdAndTenant(String userId, String locationId) {
    checkNotNull(userId, "userId is null");
    checkNotNull(locationId, "locationId is null");
    String queryString = String.format(
        "select location from %s location inner join location.cloudModel cloud inner join cloud.tenantModel tenant where tenant.userId = :tenant and location.cloudUniqueId = :id",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", userId)
        .setParameter("id", locationId);
    try {
      //noinspection unchecked
      return (LocationModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<LocationModel> findByTenantAndCloud(String tenantId, String cloudId) {
    checkNotNull(tenantId, "tenant is null");
    checkNotNull(cloudId, "cloudId is null");
    String queryString = String.format(
        "select location from %s location inner join location.cloudModel cloud inner join cloud.tenantModel tenant where tenant.userId = :tenant and cloud.cloudId = :cloudId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenantId)
        .setParameter("cloudId", cloudId);
    //noinspection unchecked
    return (List<LocationModel>) query.getResultList();
  }
}
