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

import com.google.inject.Inject;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.iaas.byon.UsageException;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import java.util.List;
import org.cloudiator.messages.General.Error;
import javax.transaction.Transactional;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.Byon.ByonNodeDeleteRequestMessage;
import org.cloudiator.messages.Byon.ByonNodeDeletedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeDeleteRequestListener  implements Runnable {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonNodeDeleteRequestListener.class);
  private final MessageInterface messageInterface;
  private final ByonNodeDomainRepository domainRepository;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public ByonNodeDeleteRequestListener(MessageInterface messageInterface,
      ByonNodeDomainRepository domainRepository) {
    this.messageInterface = messageInterface;
    this.domainRepository = domainRepository;
  }

  @Override
  public void run() {
    subscription = messageInterface.subscribe(ByonNodeDeleteRequestMessage.class,
        ByonNodeDeleteRequestMessage.parser(),
        (requestId, request) -> {
          try {
            Byon.ByonNode messageNode = request.getByonNode();
            String id = messageNode.getId();
            LOGGER.debug(String.format("%s retrieved request to delete"
                + "byon node with id %s.", this, id));
            ByonNode node = ByonToByonMessageConverter.INSTANCE.applyBack(messageNode);
            deleteByonNode(node);
            LOGGER.info("byon node deleted. sending response");
            messageInterface.reply(requestId,
                ByonNodeDeletedResponse.newBuilder().build());
            LOGGER.info("response sent.");
          } catch (UsageException ex) {
            LOGGER.error("Usage Exception occurred.", ex);
            sendErrorResponse(requestId, "Usage Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          } catch (Exception ex) {
            LOGGER.error("Exception occurred.", ex);
            sendErrorResponse(requestId, "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          }
        });
  }

  private void isDeletable(String id) throws UsageException {
    boolean matchingId = false;
    boolean allocated = false;
    List<ByonNode> nodes = domainRepository.find();

    for(ByonNode node: nodes) {
      matchingId = node.id().equals(id);
      allocated = node.allocated();
      if(matchingId && allocated) {
        return;
      }
    }

    if (!matchingId) {
      throw new UsageException(String.format("%s cannot delete node, as id %s"
          + "is unknown", this, id));
    }
    else {
      throw new UsageException(String.format("%s cannot delete node, as node"
          + "is already deleted.", this, id));
    }
  }

  @Transactional
  private void deleteByonNode(ByonNode node) throws UsageException {
    //allocated node must reside in the system
    isDeletable(node.id());
    domainRepository.save(node);
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeDeletedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
