package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class DiscoveryListenerWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageDiscoveryListener.class);
  private final DiscoveryQueue discoveryQueue;
  private final Set<DiscoveryListener> discoveryListeners;

  @Inject
  public DiscoveryListenerWorker(DiscoveryQueue discoveryQueue,
      Set<DiscoveryListener> discoveryListeners) {
    this.discoveryQueue = discoveryQueue;
    this.discoveryListeners = discoveryListeners;
  }

  private Set<DiscoveryListener> interestedIn(Discovery discovery) {
    return discoveryListeners.stream().filter(
        discoveryListener -> discoveryListener.interestedIn().isAssignableFrom(discovery.getType()))
        .collect(Collectors.toSet());
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        Discovery discovery = discoveryQueue.take();
        interestedIn(discovery).forEach(
            discoveryListener -> discoveryListener.handle(discovery.discovery()));
      } catch (InterruptedException e) {
        LOGGER.warn(String.format("%s got interrupted.", this), e);
        Thread.currentThread().interrupt();
      }
    }
  }
}
