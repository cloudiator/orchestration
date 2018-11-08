package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;

/**
 * Created by daniel on 01.06.17.
 */
public class HardwareDiscoveryWorker extends AbstractDiscoveryWorker<HardwareFlavor> {

  @Inject
  public HardwareDiscoveryWorker(DiscoveryQueue discoveryQueue,
      DiscoveryService discoveryService, DiscoveryErrorHandler discoveryErrorHandler) {
    super(discoveryQueue, discoveryService, discoveryErrorHandler);
  }

  @Override
  protected Iterable<HardwareFlavor> resources(DiscoveryService discoveryService) {
    return discoveryService.listHardwareFlavors();
  }
}
