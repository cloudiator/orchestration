package io.github.cloudiator.iaas.discovery;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 25.01.17.
 */
public abstract class AbstractDiscoveryWorker<T> implements Schedulable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiscoveryWorker.class);

  private final DiscoveryQueue discoveryQueue;
  private final DiscoveryService discoveryService;

  @Inject
  public AbstractDiscoveryWorker(DiscoveryQueue discoveryQueue, DiscoveryService discoveryService) {
    checkNotNull(discoveryQueue, "discoveryQueue is null");
    this.discoveryQueue = discoveryQueue;
    checkNotNull(discoveryService, "discoveryService is null");
    this.discoveryService = discoveryService;
  }

  protected abstract Iterable<T> resources(DiscoveryService discoveryService);

  @Override
  public final long period() {
    return 60;
  }

  @Override
  public final long delay() {
    return 0;
  }

  @Override
  public final TimeUnit timeUnit() {
    return TimeUnit.SECONDS;
  }

  @Override
  public void run() {
    LOGGER.debug(String.format("%s is starting new discovery run", this));
    try {
      StreamSupport.stream(resources(discoveryService).spliterator(), false).map(Discovery::new)
          .forEach(discoveryQueue::add);
    } catch (Exception e) {
      LOGGER.error(String.format(
          "%s reported exception %s during discovery run. Exception was caught to allow further executions.",
          this, e.getMessage()), e);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("discoveryQueue", discoveryQueue).toString();
  }
}
