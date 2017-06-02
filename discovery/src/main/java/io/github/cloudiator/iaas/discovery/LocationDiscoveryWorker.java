package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationDiscoveryWorker extends AbstractDiscoveryWorker<Location> {

  @Inject
  public LocationDiscoveryWorker(DiscoveryQueue discoveryQueue,
      DiscoveryService discoveryService) {
    super(discoveryQueue, discoveryService);
  }

  @Override
  protected Iterable<Location> resources(DiscoveryService discoveryService) {
    return discoveryService.listLocations();
  }
}
