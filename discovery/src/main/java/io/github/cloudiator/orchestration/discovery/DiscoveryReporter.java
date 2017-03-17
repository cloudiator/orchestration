package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.domain.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryReporter implements Runnable {

    private final DiscoveryReportingInterface discoveryReportingInterface;
    private final DiscoveryQueue discoveryQueue;

    private final static Logger LOGGER = LoggerFactory.getLogger(DiscoveryReporter.class);

    public DiscoveryReporter(DiscoveryReportingInterface discoveryReportingInterface,
        DiscoveryQueue discoveryQueue) {
        checkNotNull(discoveryQueue, "discoveryQueue is null");
        this.discoveryQueue = discoveryQueue;
        checkNotNull(discoveryReportingInterface, "discoveryReportingInterface is null");
        this.discoveryReportingInterface = discoveryReportingInterface;
    }

    @Override public void run() {
        while (true) {
            try {
                final Resource resource = discoveryQueue.take();
                discoveryReportingInterface.report(resource);
            } catch (InterruptedException e) {
                LOGGER.error(String
                    .format("%s got interrupted while waiting for new discoveries to report.",
                        this), e);
            }
        }
    }
}
