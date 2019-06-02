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

package io.github.cloudiator.iaas.byon.messaging;

import static com.google.common.base.Preconditions.checkState;

import io.github.cloudiator.domain.ByonIO;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.Byon.AddByonNodeRequest;
import org.cloudiator.messages.Byon.ByonNodeAddedResponse;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddByonNodeSubscriber implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AddByonNodeSubscriber.class);
  private final MessageInterface messageInterface;
  private final ByonPublisher publisher;
  private final ByonNodeDomainRepository domainRepository;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public AddByonNodeSubscriber(MessageInterface messageInterface,
      ByonPublisher publisher, ByonNodeDomainRepository domainRepository) {
    this.messageInterface = messageInterface;
    this.publisher = publisher;
    this.domainRepository = domainRepository;
  }

  @Override
  public void run() {
    subscription = messageInterface.subscribe(AddByonNodeRequest.class,
        AddByonNodeRequest.parser(), (requestId, request) -> {
          try {
            Byon.ByonData data = request.getByonRequest();
            checkState(!data.getAllocated(),"cannot add a node with state allocated");
            Byon.ByonNode byonNode = ByonOperations.buildMessageNode(request.getUserId(), data);
            ByonNode node = ByonToByonMessageConverter.INSTANCE.applyBack(byonNode);
            persistNode(node);
            LOGGER.info("byon node registered. sending response");
            sendSuccessResponse(requestId, node);
            LOGGER.info("response sent.");
            publisher.publishEvent(request.getUserId(), data, ByonIO.ADD);
          } catch (Exception ex) {
            LOGGER.error("Exception occurred.", ex);
            AddByonNodeSubscriber.this.sendErrorResponse(requestId,
                "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          }
        });
  }

  @Transactional
  private void persistNode(ByonNode node) {
    domainRepository.save(node);
  }

  private final void sendSuccessResponse(String messageId, ByonNode node) {
    messageInterface.reply(messageId,
        ByonNodeAddedResponse.newBuilder().
            setByonNode(ByonToByonMessageConverter.INSTANCE.apply(node)).build());
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeAddedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }

  void terminate() {
    if (subscription != null) {
      subscription.cancel();
    }
  }
}
