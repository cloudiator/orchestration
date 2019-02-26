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
import io.github.cloudiator.persistance.CloudDomainRepository;
import java.util.Optional;
import org.cloudiator.messages.Cloud.CloudDeletedResponse;
import org.cloudiator.messages.Cloud.DeleteCloudRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteCloudSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCloudSubscriber.class);
  private final MessageInterface messageInterface;
  private final CloudDomainRepository cloudDomainRepository;
  private final CloudStateMachine cloudStateMachine;


  @Inject
  public DeleteCloudSubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository,
      CloudStateMachine cloudStateMachine) {
    this.messageInterface = messageInterface;
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudStateMachine = cloudStateMachine;
  }

  @Override
  public void run() {
    messageInterface.subscribe(DeleteCloudRequest.class, DeleteCloudRequest.parser(),
        (messageId, deleteCloudRequest) -> {
          try {

            final Optional<ExtendedCloud> cloud = retrieveCloud(
                deleteCloudRequest.getUserId(), deleteCloudRequest.getCloudId());

            if (!cloud.isPresent()) {
              messageInterface.reply(messageId, Error.newBuilder().setCode(404)
                  .setMessage(String
                      .format("Cloud with id %s does not exist.", deleteCloudRequest.getCloudId()))
                  .build());
              return;
            }

            cloudStateMachine.apply(cloud.get(), CloudState.DELETED, new Object[0]);

            messageInterface.reply(messageId, CloudDeletedResponse.newBuilder().build());

          } catch (Exception e) {
            LOGGER.error("Unexpected exception during cloud deletion. Error was " + e.getMessage(),
                e);
            messageInterface.reply(messageId, Error.newBuilder().setCode(500).setMessage(
                "Unexpected exception during cloud deletion. Error was " + e.getMessage()).build());
          }
        });
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Optional<ExtendedCloud> retrieveCloud(String userId, String cloudId) {
    return Optional.ofNullable(cloudDomainRepository.findByUserAndId(userId, cloudId));
  }


}
