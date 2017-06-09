/*
 * Copyright 2017 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.iaas.common.persistance.repositories;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.iaas.common.persistance.entities.ResourceModel;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 21.06.15.
 */
public class BaseResourceRepositoryJpa<T extends ResourceModel>
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
    checkNotNull("tenant is null");
    String queryString = String.format(
        "select resource from %s resource inner join resource.cloud cloud inner join cloud.tenant ct where ct.userId=:tenant",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant);
    //noinspection unchecked
    return (List<T>) query.getResultList();
  }
}
