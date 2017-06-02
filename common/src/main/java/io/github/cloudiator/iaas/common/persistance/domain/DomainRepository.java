package io.github.cloudiator.iaas.common.persistance.domain;

import java.util.List;

/**
 * Created by daniel on 31.05.17.
 */
public interface DomainRepository<T> {

  T findById(String id);

  void save(T t);

  void delete(T t);

  List<T> findAll();

}
