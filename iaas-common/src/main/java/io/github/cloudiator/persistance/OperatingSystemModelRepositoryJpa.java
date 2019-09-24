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
import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersion;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

class OperatingSystemModelRepositoryJpa extends
    BaseModelRepositoryJpa<OperatingSystemModel> implements OperatingSystemModelRepository {

  @Inject
  protected OperatingSystemModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<OperatingSystemModel> type) {
    super(entityManager, type);
  }

  @Nullable
  @Override
  public OperatingSystemModel findByArchitectureFamilyVersion(OperatingSystemArchitecture architecture, OperatingSystemFamily family, OperatingSystemVersion version) {
    String queryStringWithVersion = String
            .format(
                    "from %s where operatingSystemArchitecture=:operatingSystemArchitecture and operatingSystemFamily=:operatingSystemFamily and version=:version",
                    type.getName());
    String queryStringWithoutVersion = String
            .format(
                    "from %s where operatingSystemArchitecture=:operatingSystemArchitecture and operatingSystemFamily=:operatingSystemFamily and version is null",
                    type.getName());

    Query query;
    if (version.version() == null) {
      query = em().createQuery(queryStringWithoutVersion);
    } else {
      query = em().createQuery(queryStringWithVersion);
      query.setParameter("version", version.version());
    }

    query
            .setParameter("operatingSystemArchitecture", architecture)
            .setParameter("operatingSystemFamily", family);
    try {
      //noinspection unchecked
      return (OperatingSystemModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}
