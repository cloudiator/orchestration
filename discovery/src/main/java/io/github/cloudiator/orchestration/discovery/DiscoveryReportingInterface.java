package io.github.cloudiator.orchestration.discovery;


import de.uniulm.omi.cloudiator.sword.domain.Resource;

/**
 * Created by daniel on 25.01.17.
 */
public interface DiscoveryReportingInterface {

    void report(Resource resource);

}
