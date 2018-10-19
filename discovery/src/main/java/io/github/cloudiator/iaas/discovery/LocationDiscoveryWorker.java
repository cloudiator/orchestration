package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationDiscoveryWorker extends AbstractDiscoveryWorker<Location> {

  @Inject
  public LocationDiscoveryWorker(DiscoveryQueue discoveryQueue,
      DiscoveryService discoveryService, DiscoveryErrorHandler discoveryErrorHandler) {
    super(discoveryQueue, discoveryService, discoveryErrorHandler);
  }

  @Override
  protected Iterable<Location> resources(DiscoveryService discoveryService) {
    return discoveryService.listLocations();
  }
}
