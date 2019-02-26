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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import io.github.cloudiator.persistance.CloudDomainRepository;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messages.Cloud.CloudQueryResponse;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 09.06.17.
 */
public class CloudQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloudQuerySubscriber.class);
  private static final CloudMessageToCloudConverter CLOUD_CONVERTER = CloudMessageToCloudConverter.INSTANCE;
  private final MessageInterface messageInterface;
  private final CloudDomainRepository cloudDomainRepository;

  @Inject
  public CloudQuerySubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository) {
    checkNotNull(cloudDomainRepository, "cloudDomainRepository is null");
    this.cloudDomainRepository = cloudDomainRepository;
    checkNotNull(messageInterface, "messageInterface is null");
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    Subscription subscription = messageInterface
        .subscribe(CloudQueryRequest.class, CloudQueryRequest.parser(),
            (requestId, request) -> {

              try {
                decideAndReply(requestId, request);
              } catch (Exception e) {
                LOGGER.error(
                    String.format("Exception %s caught while execution %s", e.getMessage(), this),
                    e);
              }
            });
  }

  private void decideAndReply(String requestId, CloudQueryRequest request) {
    if (request.getUserId().isEmpty()) {
      replyErrorNoUserId(requestId);
      return;
    }
    if (!request.getCloudId().isEmpty()) {
      replyForUserIdAndCloudId(requestId, request.getUserId(), request.getCloudId());
      return;
    }

    replyForUserId(requestId, request.getUserId());
  }

  private void replyErrorNoUserId(String requestId) {
    messageInterface.reply(CloudQueryResponse.class, requestId,
        Error.newBuilder().setCode(500).setMessage("Request does not contain userId.")
            .build());
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    final ExtendedCloud cloud = cloudDomainRepository
        .findByUserAndId(userId, cloudId);
    if (cloud == null) {
      messageInterface.reply(CloudQueryResponse.class, requestId,
          Error.newBuilder().setCode(404)
              .setMessage(String.format("Cloud with id %s was not found.", cloudId))
              .build());
    } else {
      CloudQueryResponse cloudQueryResponse = CloudQueryResponse.newBuilder()
          .addClouds(CLOUD_CONVERTER.applyBack(cloud)).build();
      messageInterface.reply(requestId, cloudQueryResponse);
    }

  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void replyForUserId(String requestId, String userId) {
    CloudQueryResponse cloudQueryResponse = CloudQueryResponse.newBuilder()
        .addAllClouds(cloudDomainRepository.findAll(userId).stream().map(
            CLOUD_CONVERTER::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, cloudQueryResponse);
  }

}
