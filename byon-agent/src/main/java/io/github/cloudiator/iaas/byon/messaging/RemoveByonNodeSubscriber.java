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
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.iaas.byon.UsageException;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.Byon.ByonData;
import org.cloudiator.messages.Byon.ByonNodeRemovedResponse;
import org.cloudiator.messages.Byon.RemoveByonNodeRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveByonNodeSubscriber  implements Runnable {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(RemoveByonNodeSubscriber.class);
  private final MessageInterface messageInterface;
  private final ByonPublisher publisher;
  private final ByonNodeDomainRepository domainRepository;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public RemoveByonNodeSubscriber(MessageInterface messageInterface,
      ByonPublisher publisher,
      ByonNodeDomainRepository domainRepository) {
    this.messageInterface = messageInterface;
    this.publisher = publisher;
    this.domainRepository = domainRepository;
  }

  @Override
  public void run() {
    subscription = messageInterface.subscribe(RemoveByonNodeRequest.class,
        RemoveByonNodeRequest.parser(),
        (requestId, request) -> {
          try {
            final String id = request.getId();
            final String userId = request.getUserId();
            checkState(
                !ByonOperations.isAllocatedCheck(domainRepository, id, userId),
                String.format(
                    "Cannot remove node with id %s "
                        + "as it seems to be allocated at the moment", id));
            LOGGER.debug(String.format("%s retrieved request to remove "
                    + "byon node with id %s.", this, id));
            removeByonNode(id, userId);
            LOGGER.info("byon node removed. sending response");
            messageInterface.reply(requestId,
                ByonNodeRemovedResponse.newBuilder().build());
            LOGGER.info("response sent.");
            // Set only id, userId, unallocated and REMOVE as information
            Byon.ByonData data = ByonData.newBuilder().setAllocated(false).build();
            publisher.publishEvent(userId, data, ByonIO.EVICT);
          } catch (Exception ex) {
            LOGGER.error("Exception occurred.", ex);
            sendErrorResponse(requestId, "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          }
        });
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void removeByonNode(String id, String userId) throws UsageException {
    domainRepository.delete(id);
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeRemovedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
