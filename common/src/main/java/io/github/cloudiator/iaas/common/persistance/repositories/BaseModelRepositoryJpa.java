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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.iaas.common.persistance.entities.Model;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Created by daniel on 31.10.14.
 */
public class BaseModelRepositoryJpa<T extends Model> implements ModelRepository<T> {

  protected final Class<T> type;
  @SuppressWarnings("unused")
  private final Provider<EntityManager> entityManager;

  @Inject
  BaseModelRepositoryJpa(Provider<EntityManager> entityManager, TypeLiteral<T> type) {
    //noinspection unchecked
    this.type = (Class<T>) type.getRawType();
    this.entityManager = entityManager;
  }

  EntityManager em() {
    //todo: replace with correct call to jpaAPI
    //todo: currently blocked by https://github.com/playframework/playframework/issues/4890
    return entityManager.get();
  }

  @Override
  @Nullable
  public T findById(Long id) {
    checkNotNull(id);
    return em().find(type, id);
  }

  private void persist(final T t) {
    em().persist(t);
  }

  @Override
  public void save(final T t) {
    checkNotNull(t);
    if (t.getId() == null) {
      this.persist(t);
    } else {
      this.update(t);
    }
    this.flush();
    this.refresh(t);
  }

  protected T update(final T t) {
    return em().merge(t);
  }

  private void flush() {
    em().flush();
  }

  private T refresh(final T t) {
    em().refresh(t);
    return t;
  }

  @Override
  public void delete(final T t) {
    checkNotNull(t);
    em().remove(t);
  }


  @Override
  public List<T> findAll() {
    String queryString = String.format("from %s", type.getName());
    Query query = em().createQuery(queryString);
    //noinspection unchecked
    return query.getResultList();
  }
}
