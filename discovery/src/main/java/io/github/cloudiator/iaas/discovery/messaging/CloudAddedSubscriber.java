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

package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.iaas.discovery.CloudStateMachine;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import io.github.cloudiator.messaging.InitializeCloudFromNewCloud;
import io.github.cloudiator.persistance.CloudDomainRepository;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Cloud.CloudCreatedResponse;
import org.cloudiator.messages.Cloud.CreateCloudRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudAddedSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloudAddedSubscriber.class);
  private static final InitializeCloudFromNewCloud INITIALIZE_CLOUD_FROM_NEW_CLOUD = InitializeCloudFromNewCloud.INSTANCE;
  private final MessageInterface messageInterface;
  private final CloudDomainRepository cloudDomainRepository;
  private final CloudStateMachine cloudStateMachine;

  @Inject
  public CloudAddedSubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository,
      CloudStateMachine cloudStateMachine) {
    this.messageInterface = messageInterface;
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudStateMachine = cloudStateMachine;
  }

  @Override
  public void run() {

    final Subscription subscription = messageInterface
        .subscribe(CreateCloudRequest.class, CreateCloudRequest.parser(),
            this::doWork);
  }

  private void doWork(String messageId, CreateCloudRequest createCloudRequest) {

    try {

      ExtendedCloud cloudToBeCreated = INITIALIZE_CLOUD_FROM_NEW_CLOUD
          .apply(createCloudRequest.getCloud(), createCloudRequest.getUserId());

      if (exists(cloudToBeCreated)) {
        messageInterface.reply(CloudCreatedResponse.class, messageId,
            Error.newBuilder().setCode(409).setMessage(String
                .format("The cloud %s is already registered",
                    cloudToBeCreated)).build());
      }

      try {
        final ExtendedCloud createdCloud = cloudStateMachine
            .apply(cloudToBeCreated, CloudState.OK);

        final CloudCreatedResponse cloudCreatedResponse = CloudCreatedResponse.newBuilder()
            .setCloud(
                CloudMessageToCloudConverter.INSTANCE.applyBack(createdCloud)).build();
        messageInterface.reply(messageId, cloudCreatedResponse);

      } catch (ExecutionException e) {
        if (e.getCause() instanceof Exception) {
          throw (Exception) e.getCause();
        }
        throw e;
      }

    } catch (Exception e) {
      LOGGER.error(String.format("Unexpected exception occurred during handling of request %s.",
          createCloudRequest), e);
      messageInterface.reply(CloudCreatedResponse.class, messageId, Error.newBuilder()
          .setMessage(String
              .format("Unexpected exception occurred during handling of request %s: %s.",
                  createCloudRequest, e.getMessage()))
          .setCode(500).build());
    }
  }


  @SuppressWarnings("WeakerAccess")
  @Transactional
  boolean exists(ExtendedCloud extendedCloud) {
    return cloudDomainRepository.findById(extendedCloud.id()) != null;
  }

}
