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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;

class ByonNodeModelRepositoryJpa  extends BaseModelRepositoryJpa<ByonNodeModel> implements
    ByonNodeModelRepository {

  @Inject
  protected ByonNodeModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<ByonNodeModel> type) {
    super(entityManager, type);
  }

  @Override
  public List<ByonNodeModel> getByTenant(String userId) {
    checkNotNull(userId, "userId is null");
    String queryString = String
        .format(
            "select byonNode from %s byonNode inner join byonNode.tenantModel tenant where tenant.userId = :userId",
            type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId);
    //noinspection unchecked
    return query.getResultList();
  }

  @Nullable
  @Override
  public ByonNodeModel getByTenantAndDomainId(String userId, String domainId) {
    checkNotNull(userId, "userId is null");
    checkNotNull(domainId, "domainId is null");
    String queryString = String.format(
        "select byonNode from %s byonNode inner join byonNode.tenantModel tenant where tenant.userId = :userId and byonNode.domainId = :domainId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("userId", userId)
        .setParameter("domainId", domainId);
    @SuppressWarnings("unchecked") List<ByonNodeModel> byonNodes = query.getResultList();
    return byonNodes.stream().findFirst().orElse(null);
  }

  @Nullable
  @Override
  public ByonNodeModel getByDomainId(String domainId) {
    checkNotNull(domainId, "domainId is null");
    String queryString = String
        .format("select byonNode from %s byonNode where byonNode.domainId = :domainId", type.getName());
    Query query = em().createQuery(queryString).setParameter("domainId", domainId);
    @SuppressWarnings("unchecked") List<ByonNodeModel> byonNodes = query.getResultList();
    return byonNodes.stream().findFirst().orElse(null);
  }
}
