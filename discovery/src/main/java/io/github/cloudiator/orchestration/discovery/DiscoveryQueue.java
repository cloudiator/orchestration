package io.github.cloudiator.orchestration.discovery;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.Resource;
import de.uniulm.omi.cloudiator.util.execution.SimpleBlockingQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryQueue implements SimpleBlockingQueue<Resource> {

    private final BlockingQueue<Resource> blockingQueue;

    public DiscoveryQueue() {
        this.blockingQueue = new LinkedBlockingQueue<>();
    }

    @Override public void add(Resource resource) {
        this.blockingQueue.add(resource);
    }

    @Override public Resource take() throws InterruptedException {
        return this.blockingQueue.take();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
