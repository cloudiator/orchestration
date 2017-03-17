package io.github.cloudiator.orchestration.discovery;


import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 25.01.17.
 */
public class ResourceSuppliers {

    private final DiscoveryService discoveryService;

    public ResourceSuppliers(DiscoveryService discoveryService) {
        checkNotNull(discoveryService);
        this.discoveryService = discoveryService;
    }

    public ResourceSupplier imageSupplier() {
        return new AbstractResourceSupplier<Image>(discoveryService) {
            @Override protected Iterable<Image> resources(DiscoveryService discoveryService) {
                return discoveryService.listImages();
            }
        };
    }
}
