package io.github.cloudiator.messaging;

import java.util.List;

public interface MessageRepository<T> {

  public T getById(String userId, String id);

  public List<T> getAll(String userId);

}
