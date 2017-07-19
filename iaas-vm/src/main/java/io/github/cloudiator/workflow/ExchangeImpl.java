package io.github.cloudiator.workflow;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Created by daniel on 07.02.17.
 */
public class ExchangeImpl implements Exchange {

  @Nullable
  private final Object data;
  private final static String ILLEGAL_CLASS = "Caller expected class %s but object %s was of type %s.";

  public ExchangeImpl(@Nullable Object data) {
    this.data = data;
  }

  @Override
  public Optional<Object> getData() {
    return Optional.ofNullable(data);
  }

  @Override
  public <T> Optional<T> getData(Class<T> clazz) {

    if (data == null) {
      return Optional.empty();
    }

    try {
      final T cast = clazz.cast(data);
      return Optional.of(cast);
    } catch (ClassCastException e) {
      throw new IllegalStateException(String.format(ILLEGAL_CLASS, clazz, data, data.getClass()),
          e);
    }
  }
}
