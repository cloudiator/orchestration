package io.github.cloudiator.workflow;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Created by daniel on 07.02.17.
 */
public class ExchangeImpl implements Exchange {

    @Nullable private final Object data;

    public ExchangeImpl(@Nullable Object data) {
        this.data = data;
    }

    @Override public Optional<Object> getData() {
        return Optional.ofNullable(data);
    }

    @Override public <T> Optional<T> getData(Class<T> clazz) {


        return null;
    }
}
