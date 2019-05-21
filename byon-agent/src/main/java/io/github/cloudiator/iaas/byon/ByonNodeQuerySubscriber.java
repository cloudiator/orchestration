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


package io.github.cloudiator.iaas.byon;

import com.google.inject.Inject;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Byon.ByonNodeQueryRequest;
import org.cloudiator.messages.Byon.ByonNodeQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeQuerySubscriber implements Runnable {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonNodeQuerySubscriber.class);
  private final MessageInterface messageInterface;
  private final ByonNodeDomainRepository domainRepository;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public ByonNodeQuerySubscriber(MessageInterface messageInterface,
      ByonNodeDomainRepository domainRepository) {
    this.messageInterface = messageInterface;
    this.domainRepository = domainRepository;
  }

  @Override
  public void run() {
    subscription = messageInterface.subscribe(ByonNodeQueryRequest.class,
        ByonNodeQueryRequest.parser(),
        (requestId, request) -> {
          try {
            String id = request.getId();
            ByonNodeQueryResponse byonNodeQueryResponse = ByonNodeQueryResponse.newBuilder()
                .setByonNode(ByonToByonMessageConverter.INSTANCE.apply(domainRepository.findById(id)))
                .build();
            messageInterface.reply(requestId, byonNodeQueryResponse);
          } catch (Exception ex) {
            LOGGER.error("Exception occurred.", ex);
            sendErrorResponse(requestId, "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          }
        });
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeQueryResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
