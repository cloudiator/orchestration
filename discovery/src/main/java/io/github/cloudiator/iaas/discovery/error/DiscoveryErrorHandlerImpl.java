package io.github.cloudiator.iaas.discovery.error;

import de.uniulm.omi.cloudiator.sword.multicloud.exception.MultiCloudException;

public class DiscoveryErrorHandlerImpl implements DiscoveryErrorHandler {

  @Override
  public void report(MultiCloudException multiCloudException) {
    this.report(multiCloudException.cloudId(), multiCloudException.exception());
  }

  @Override
  public void report(String cloudId, Exception e) {
    //currently no-op
  }
}
