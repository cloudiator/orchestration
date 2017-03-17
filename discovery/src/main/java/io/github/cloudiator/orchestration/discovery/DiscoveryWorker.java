package io.github.cloudiator.orchestration.discovery;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.Resource;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryWorker implements Schedulable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryWorker.class);

    private final ResourceSupplier resourceSupplier;
    private final DiscoveryQueue discoveryQueue;

    public DiscoveryWorker(ResourceSupplier resourceSupplier, DiscoveryQueue discoveryQueue) {
        checkNotNull(discoveryQueue, "discoveryQueue is null");
        this.discoveryQueue = discoveryQueue;
        checkNotNull(resourceSupplier, "resourceSupplier is null");
        this.resourceSupplier = resourceSupplier;
    }

    @Override public long period() {
        return 60;
    }

    @Override public long delay() {
        return 0;
    }

    @Override public TimeUnit timeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override public void run() {
        LOGGER.debug(String.format("%s is starting new discovery run", this));
        resourceSupplier.get().forEach((Consumer<Resource>) discoveryQueue::add);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("resourceSupplier", resourceSupplier)
            .add("discoveryQueue", discoveryQueue).toString();
    }
}
