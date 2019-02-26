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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

class ApiModelRepositoryJpa extends BaseModelRepositoryJpa<ApiModel> implements
    ApiModelRepository {

  @Inject
  protected ApiModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<ApiModel> type) {
    super(entityManager, type);
  }

  @Override
  @Nullable
  public ApiModel findByProviderName(String providerName) {
    String query = String.format("from %s where providerName=:p", type.getName());
    @SuppressWarnings("unchecked") List<ApiModel> models = em().createQuery(query)
        .setParameter("p", providerName).getResultList();
    if (models.isEmpty()) {
      return null;
    }
    return models.get(0);
  }
}
