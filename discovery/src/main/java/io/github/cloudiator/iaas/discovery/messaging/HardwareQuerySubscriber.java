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

import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import io.github.cloudiator.messaging.HardwareMessageToHardwareConverter;
import io.github.cloudiator.persistance.HardwareDomainRepository;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messages.Hardware.HardwareQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class HardwareQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(HardwareQuerySubscriber.class);
  private static final HardwareMessageToHardwareConverter HARDWARE_CONVERTER = HardwareMessageToHardwareConverter.INSTANCE;
  private final MessageInterface messageInterface;
  private final HardwareDomainRepository hardwareDomainRepository;

  @Inject
  public HardwareQuerySubscriber(MessageInterface messageInterface,
      HardwareDomainRepository hardwareDomainRepository) {
    this.messageInterface = messageInterface;
    this.hardwareDomainRepository = hardwareDomainRepository;
  }


  @Override
  public void run() {
    Subscription subscription = messageInterface
        .subscribe(HardwareQueryRequest.class, HardwareQueryRequest.parser(),
            (requestId, hardwareQueryRequest) -> {

              try {
                decideAndReply(requestId, hardwareQueryRequest);
              } catch (Exception e) {
                LOGGER.error(String
                    .format("Caught exception %s during execution of %s", e.getMessage(), this), e);
              }
            });
  }

  private void decideAndReply(String requestId, HardwareQueryRequest request) {
    if (request.getUserId().isEmpty()) {
      replyErrorNoUserId(requestId);
      return;
    }
    if (!request.getHardwareId().isEmpty()) {
      replyForUserIdAndHardwareId(requestId, request.getUserId(), request.getHardwareId());
      return;
    }
    if (!request.getCloudId().isEmpty()) {
      replyForUserIdAndCloudId(requestId, request.getUserId(), request.getCloudId());
      return;
    }
    replyForUserId(requestId, request.getUserId());
  }

  private void replyErrorNoUserId(String requestId) {
    messageInterface.reply(HardwareQueryResponse.class, requestId,
        Error.newBuilder().setCode(500).setMessage("Request does not contain userId.")
            .build());
  }


  private void replyForUserIdAndHardwareId(String requestId, String userId, String hardwareId) {
    final HardwareFlavor hardwareFlavor = hardwareDomainRepository
        .findByTenantAndId(userId, hardwareId);
    if (hardwareFlavor == null) {

      messageInterface.reply(requestId, HardwareQueryResponse.newBuilder().build());
    } else {
      HardwareQueryResponse hardwareQueryResponse = HardwareQueryResponse.newBuilder()
          .addHardwareFlavors(HARDWARE_CONVERTER.applyBack(hardwareFlavor)).build();
      messageInterface.reply(requestId, hardwareQueryResponse);
    }

  }

  private void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    HardwareQueryResponse hardwareQueryResponse = HardwareQueryResponse.newBuilder()
        .addAllHardwareFlavors(
            hardwareDomainRepository.findByTenantAndCloud(userId, cloudId).stream().map(
                HARDWARE_CONVERTER::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, hardwareQueryResponse);
  }

  private void replyForUserId(String requestId, String userId) {
    HardwareQueryResponse hardwareQueryResponse = HardwareQueryResponse.newBuilder()
        .addAllHardwareFlavors(hardwareDomainRepository.findAll(userId).stream().map(
            HARDWARE_CONVERTER::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, hardwareQueryResponse);
  }

}
