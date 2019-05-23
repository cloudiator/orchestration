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
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.iaas.byon.UsageException;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import java.util.List;
import org.cloudiator.messages.General.Error;
import javax.transaction.Transactional;
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
                checkState(
                    data.getAllocated(),
                    String.format(
                        "setting %s node's state to allocated"
                            + "is not possible due to the requesting node state being unallocated",
                        data.getName()));
                String id = messageNode.getId();
                checkState(ByonOperations.allocatedStateChanges(domainRepository, id, true),
                    ByonOperations.wrongStateChangeMessage(true, id));
                LOGGER.debug(
                    String.format(
                        "%s retrieved request to allocate" + "byon node with id %s.", this, id));
                ByonNode node = ByonToByonMessageConverter.INSTANCE.applyBack(messageNode);
                allocateByonNode(node);
                LOGGER.info("byon node allocated. sending response");
                messageInterface.reply(requestId, ByonNodeAllocatedResponse.newBuilder().build());
                LOGGER.info("response sent.");
                publisher.publishEvent(messageNode.getNodeData());
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

  private void isAllocatable(String id) throws UsageException {
    boolean matchingId = false;
    boolean unAllocated = false;
    List<ByonNode> nodes = domainRepository.find();

    for(ByonNode node: nodes) {
      matchingId = node.id().equals(id);
      unAllocated = !node.allocated();
      if(matchingId && unAllocated) {
        return;
      }
    }

    if (!matchingId) {
      throw new UsageException(String.format("%s cannot allocate node, as id %s"
          + "is unknown", this, id));
    }
    else {
      throw new UsageException(String.format("%s cannot allocate node, as node"
          + "is already allocated", this, id));
    }
  }

  @Transactional
  private void allocateByonNode(ByonNode node) throws UsageException {
    //unallocated node must reside in the system
    isAllocatable(node.id());
    domainRepository.save(node);
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeAllocatedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
