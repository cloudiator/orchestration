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

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.ByonIO;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.iaas.byon.UsageException;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import io.github.cloudiator.iaas.byon.util.IdCreator;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import java.util.List;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.Byon.ByonNodeAllocateRequestMessage;
import org.cloudiator.messages.Byon.ByonNodeAllocatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeAllocateRequestListener implements Runnable {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonNodeAllocateRequestListener.class);
  private final MessageInterface messageInterface;
  private final ByonPublisher publisher;
  private final ByonNodeDomainRepository domainRepository;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public ByonNodeAllocateRequestListener(MessageInterface messageInterface,
      ByonPublisher publisher,
      ByonNodeDomainRepository domainRepository) {
    this.messageInterface = messageInterface;
    this.publisher = publisher;
    this.domainRepository = domainRepository;
  }

  @Override
  public void run() {
    subscription =
        messageInterface.subscribe(
            ByonNodeAllocateRequestMessage.class,
            ByonNodeAllocateRequestMessage.parser(),
            (requestId, request) -> {
              try {
                Byon.ByonNode messageNode = request.getByonNode();
                Byon.ByonData data = messageNode.getNodeData();
                //nodeStateMachine already set the equivalent node to running
                checkState(
                    data.getAllocated(),
                    String.format(
                        "setting %s node's state to allocated"
                            + " is not possible due to the requesting node state being unallocated",
                        data.getName()));
                String id = IdCreator.createId(data);
                String userId = messageNode.getUserId();
                checkState(ByonOperations.allocatedStateChanges(domainRepository, id, userId,true),
                    ByonOperations.wrongStateChangeMessage(true, id));
                LOGGER.debug(
                    String.format(
                        "%s retrieved request to allocate byon node with id %s and userId %s.", this, id, userId));
                ByonNode node = ByonToByonMessageConverter.INSTANCE.applyBack(messageNode);
                //todo: create logic to distinguish between id created by node-agent and id created by IdCreator.createID(...)
                ByonNode allocateNode = ByonOperations.buildNodewithOriginalId(node, id);
                allocateByonNode(allocateNode);
                LOGGER.info("byon node allocated. sending response");
                messageInterface.reply(requestId, ByonNodeAllocatedResponse.newBuilder().build());
                LOGGER.info("response sent.");
                publisher.publishEvent(userId, data, ByonIO.UPDATE);
              } catch (UsageException ex) {
                LOGGER.error("Usage Exception occurred.", ex);
                sendErrorResponse(
                    requestId,
                    "Usage Exception occurred: " + ex.getMessage(),
                    Constants.SERVER_ERROR);
              } catch (Exception ex) {
                LOGGER.error("Exception occurred.", ex);
                sendErrorResponse(
                    requestId, "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
              }
            });
  }

  private void isAllocatable(String id, String userId) throws UsageException {
    ByonNode foundNode = domainRepository.findByTenantAndId(userId, id);

    if(foundNode == null) {
      throw new UsageException(String.format("%s cannot allocate node, as no node with id %s"
          + " and userId %s is known to the system", this, id, userId));
    }

    if(foundNode.allocated()) {
      throw new UsageException(String.format("%s cannot allocate node, as node"
          + " %s is already allocated", this, id));
    }
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void allocateByonNode(ByonNode node) throws UsageException {
    //unallocated node must reside in the system
    isAllocatable(node.id(), node.userId());
    domainRepository.save(node);
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeAllocatedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
