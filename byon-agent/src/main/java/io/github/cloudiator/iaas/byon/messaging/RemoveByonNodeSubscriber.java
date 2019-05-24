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
import io.github.cloudiator.domain.ByonIO;
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import javax.transaction.Transactional;
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
            String id = request.getId();
            checkState(
                !ByonOperations.isAllocated(domainRepository, id),
                String.format(
                    "Cannot delete node with id %s"
                        + "as it seems to be allocated at the moment", id));
            LOGGER.debug(String.format("%s retrieved request to delete"
                    + "byon node with id %s.", this, id));
            deleteByonNode(id);
            LOGGER.info("byon node deleted. sending response");
            messageInterface.reply(requestId,
                ByonNodeRemovedResponse.newBuilder().build());
            // Set only id, unallocated and REMOVE as information
            Byon.ByonData data = ByonData.newBuilder().setAllocated(false).build();
            publisher.publishEvent(data, ByonIO.REMOVE);
            LOGGER.info("response sent.");
          } catch (Exception ex) {
            LOGGER.error("Exception occurred.", ex);
            sendErrorResponse(requestId, "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          }
        });
  }

  @Transactional
  private void deleteByonNode(String id) {
    domainRepository.delete(id);
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeRemovedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
