package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.domain.Resource;


/**
 * Created by daniel on 25.01.17.
 */
public class CommandLineDiscoveryReportingInterface implements DiscoveryReportingInterface {

    private final ResourceToDiscoveryEvent resourceToDiscoveryEvent =
        new ResourceToDiscoveryEvent();

    @Override public void report(Resource resource) {
        System.out.println(resourceToDiscoveryEvent.apply(resource).toString());
    }
}
