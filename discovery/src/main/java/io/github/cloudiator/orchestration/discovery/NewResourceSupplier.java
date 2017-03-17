package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.domain.Resource;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 25.01.17.
 */
public class NewResourceSupplier implements ResourceSupplier {

    private final ResourceSupplier delegate;
    private final DiscoveryStore discoveryStore;

    public NewResourceSupplier(ResourceSupplier delegate, DiscoveryStore discoveryStore) {
        checkNotNull(discoveryStore, "discoveryStore is null");
        this.discoveryStore = discoveryStore;
        checkNotNull(delegate, "delegate is null");
        this.delegate = delegate;
    }

    @Override public Iterable<? extends Resource> get() {
        return StreamSupport.stream(delegate.get().spliterator(), false)
            .filter((Predicate<Resource>) discoveryStore::store).collect(Collectors.toSet());
    }
}
