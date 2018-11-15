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

package io.github.cloudiator.iaas.vm.config;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.messaging.CloudMessageRepository;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.cloudiator.messages.entities.User.TenantQueryRequest;
import org.cloudiator.messages.entities.User.TenantQueryResponse;
import org.cloudiator.messages.entities.UserEntities.Tenant;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 31.05.17.
 */
class Init {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(Init.class);
  private final PersistService persistService;
  private final CloudMessageRepository cloudMessageRepository;
  private final CloudRegistry cloudRegistry;
  private final UserService userService;

  @Inject
  Init(PersistService persistService,
      CloudMessageRepository cloudMessageRepository,
      CloudRegistry cloudRegistry, UserService userService) {
    this.persistService = persistService;
    this.cloudMessageRepository = cloudMessageRepository;
    this.cloudRegistry = cloudRegistry;
    this.userService = userService;
    run();
  }

  private void run() {
    LOGGER.info("Initializing");
    try {
      startPersistService();
      restoreCloudRegistry();
    } catch (Exception e) {
      System.err.println("Error while initializing. Message is " + e.getMessage() + ". Exiting.");
      e.printStackTrace();
      System.exit(1);
    }

  }

  private void startPersistService() {
    LOGGER.info("Starting persistence service");
    persistService.start();
  }

  private void restoreCloudRegistry() {
    LOGGER.info("Restoring cloud registry");
    try {
      final TenantQueryResponse tenants = userService
          .getTenants(TenantQueryRequest.newBuilder().build());

      for (String tenant : tenants.getTenantList().stream().map(Tenant::getTenant).peek(
          new Consumer<String>() {
            @Override
            public void accept(String s) {
              LOGGER.debug("Restoring cloud registry for tenant " + s);
            }
          })
          .collect(
              Collectors
                  .toSet())) {
        for (ExtendedCloud cloud : cloudMessageRepository.getAll(tenant)) {

          if (cloud.state().equals(CloudState.OK) && !cloudRegistry.isRegistered(cloud)) {
            cloudRegistry.register(cloud);
            LOGGER.debug(String.format("Adding cloud %s to the cloud registry.", cloud));
          } else {
            LOGGER.trace(String
                .format("Cloud %s was already registered or is not on OK state. Skipping", cloud));
          }
        }
      }


    } catch (ResponseException e) {
      throw new IllegalStateException("Could not restore initial state of cloud registry");
    }
  }

}
