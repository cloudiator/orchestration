/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryErrorHandlerImpl.class);
  private final CloudDomainRepository cloudDomainRepository;
  private final CloudStateMachine cloudStateMachine;

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

    LOGGER
        .debug(
            String.format("%s received exception %s for cloud %s", this, e.getMessage(), cloudId),
            e);

    final ExtendedCloud cloud = cloudDomainRepository.findById(cloudId);

    if (cloud == null) {
      LOGGER.warn(String
          .format("Error %s reported for cloud with id %s but this cloud no longer exists.",
              e.getMessage(), cloudId), e);
      return;
    }

    ExtendedCloud cloudWithError = ExtendedCloudBuilder.of(cloud)
        .diagnostic(e.getMessage()).build();

    try {
      //set cloud to error state
      LOGGER.info(String
          .format("%s is setting cloud with id %s to error state due to error %s.", this, cloudId,
              e.getMessage()), e);
      cloudStateMachine.apply(cloudWithError, CloudState.ERROR, new Object[0]);
    } catch (ExecutionException ex) {
      LOGGER.error(
          String
              .format("Error %s while setting cloud %s to ERROR state.", ex.getCause().getMessage(),
                  cloudWithError),
          e.getCause());
    }
  }
}
