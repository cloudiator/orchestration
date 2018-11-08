package io.github.cloudiator.iaas.discovery.error;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.multicloud.exception.MultiCloudException;
import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.ExtendedCloudBuilder;
import io.github.cloudiator.iaas.discovery.CloudStateMachine;
import io.github.cloudiator.persistance.CloudDomainRepository;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryErrorHandlerImpl implements DiscoveryErrorHandler {

  private final CloudDomainRepository cloudDomainRepository;
  private final CloudStateMachine cloudStateMachine;
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryErrorHandlerImpl.class);

  @Inject
  public DiscoveryErrorHandlerImpl(
      CloudDomainRepository cloudDomainRepository,
      CloudStateMachine cloudStateMachine) {
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudStateMachine = cloudStateMachine;
  }

  @Override
  public void report(MultiCloudException multiCloudException) {
    this.report(multiCloudException.cloudId(), multiCloudException.exception());
  }

  @Override
  public void report(String cloudId, Exception e) {

    final ExtendedCloud cloud = cloudDomainRepository.findById(cloudId);

    if (cloud == null) {
      LOGGER.warn(String
          .format("Error %s reported for cloud with id %s but this cloud no longer exists.",
              e.getMessage(), cloudId), e);
      return;
    }

    ExtendedCloud cloudWithError = ExtendedCloudBuilder.of(cloud)
        .diagnostic(e.getMessage()).build();
    //set cloud to error state
    try {
      cloudStateMachine.apply(cloudWithError, CloudState.ERROR);
    } catch (ExecutionException ex) {
      LOGGER.error(
          String
              .format("Error %s while setting cloud %s to ERROR state.", ex.getCause().getMessage(),
                  cloudWithError),
          e.getCause());
    }
  }
}
