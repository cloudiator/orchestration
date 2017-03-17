package io.github.cloudiator.workflow;

import java.util.Optional;

/**
 * Created by daniel on 03.02.17.
 */
public interface Exchange {

    static Exchange of(Object o) {
        return new ExchangeImpl(o);
    }

    Optional<Object> getData();

    <T> Optional<T> getData(Class<T> clazz);

}
