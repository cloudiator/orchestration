package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;

/**
 * Created by daniel on 01.06.17.
 */
public class ImageDiscoveryWorker extends AbstractDiscoveryWorker<Image> {

  @Inject
  public ImageDiscoveryWorker(DiscoveryQueue discoveryQueue,
      DiscoveryService discoveryService, DiscoveryErrorHandler discoveryErrorHandler) {
    super(discoveryQueue, discoveryService, discoveryErrorHandler);
  }

  @Override
  protected Iterable<Image> resources(DiscoveryService discoveryService) {
    return discoveryService.listImages();
  }
}
