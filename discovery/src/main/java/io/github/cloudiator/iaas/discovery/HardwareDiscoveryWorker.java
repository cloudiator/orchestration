package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;

/**
 * Created by daniel on 01.06.17.
 */
public class HardwareDiscoveryWorker extends AbstractDiscoveryWorker<HardwareFlavor> {

  @Inject
  public HardwareDiscoveryWorker(DiscoveryQueue discoveryQueue,
      DiscoveryService discoveryService) {
    super(discoveryQueue, discoveryService);
  }

  @Override
  protected Iterable<HardwareFlavor> resources(DiscoveryService discoveryService) {
    return discoveryService.listHardwareFlavors();
  }
}
