package io.github.cloudiator.iaas.discovery.error;

import de.uniulm.omi.cloudiator.sword.multicloud.exception.MultiCloudException;

public interface DiscoveryErrorHandler {

  void report(MultiCloudException multiCloudException);

  void report(String cloudId, Exception e);


}
