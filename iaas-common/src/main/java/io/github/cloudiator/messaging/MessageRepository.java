package io.github.cloudiator.messaging;

import java.util.List;
import javax.annotation.Nullable;

public interface MessageRepository<T> {

  @Nullable
  T getById(String userId, String id);

  List<T> getAll(String userId);
}
