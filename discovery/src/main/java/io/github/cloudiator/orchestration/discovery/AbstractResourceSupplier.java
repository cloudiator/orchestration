package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.domain.Resource;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 25.01.17.
 */
abstract class AbstractResourceSupplier<T extends Resource> implements ResourceSupplier {

    private final DiscoveryService discoveryService;

    protected AbstractResourceSupplier(DiscoveryService discoveryService) {
        checkNotNull(discoveryService, "discoveryService");
        this.discoveryService = discoveryService;
    }

    protected abstract Iterable<T> resources(DiscoveryService discoveryService);

    @Override public final Iterable<? extends Resource> get() {
        return resources(discoveryService);
    }
}
