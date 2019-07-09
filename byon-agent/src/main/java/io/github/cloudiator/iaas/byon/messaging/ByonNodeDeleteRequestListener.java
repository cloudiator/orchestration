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
import io.github.cloudiator.domain.ByonNodeBuilder;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.iaas.byon.UsageException;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import io.github.cloudiator.iaas.byon.util.IdCreator;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.messaging.NodePropertiesMessageToNodePropertiesConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import java.util.List;
import org.cloudiator.messages.General.Error;
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
  private static final NodePropertiesMessageToNodePropertiesConverter
      NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();
  private final MessageInterface messageInterface;
  private final ByonPublisher publisher;
  private final ByonNodeDomainRepository domainRepository;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public ByonNodeDeleteRequestListener(MessageInterface messageInterface,
      ByonPublisher publisher,
      ByonNodeDomainRepository domainRepository) {
    this.messageInterface = messageInterface;
    this.publisher = publisher;
    this.domainRepository = domainRepository;
  }

  @Override
  public void run() {
    subscription = messageInterface.subscribe(ByonNodeDeleteRequestMessage.class,
        ByonNodeDeleteRequestMessage.parser(),
        (requestId, request) -> {
          try {
            String userId = request.getUserId();
            NodeProperties props = NODE_PROPERTIES_CONVERTER.apply(request.getProperties());
            String id = IdCreator.createId(props);
            final boolean isAllocated = request.getAllocated();
            //nodeStateMachine already set the equivalent node to deleted
            checkState(
                !isAllocated,
                String.format(
                    "setting %s node's state to unallocated"
                        + " is not possible due to the requesting node state being allocated",
                    props.toString()));
            LOGGER.debug(String.format("%s retrieved request to delete "
                + "byon node with id %s and userId %s, Node can now "
                + "again get allocated", this, id, userId));
            ByonNode deleteNode = buildDeletedNode(id, userId);
            deleteByonNode(deleteNode);
            LOGGER.info("byon node deleted. sending response");
            messageInterface.reply(requestId,
                ByonNodeDeletedResponse.newBuilder()
                    .setNode(ByonToByonMessageConverter.INSTANCE.apply(deleteNode)).build());
            LOGGER.info("response sent.");
            publisher.publishEvent(userId, ByonToByonMessageConverter.INSTANCE.apply(deleteNode).getNodeData(), ByonIO.UPDATE);
          } catch (UsageException ex) {
            LOGGER.error("Usage Exception occurred.", ex);
            sendErrorResponse(requestId, "Usage Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          } catch (Exception ex) {
            LOGGER.error("Exception occurred.", ex);
            sendErrorResponse(requestId, "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          }
        });
  }

  void deleteByonNode(ByonNode node) throws UsageException {
    checkState(ByonOperations
        .allocatedStateChanges(domainRepository, node.id(), node.userId(), false));
    persist(node);
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void persist(ByonNode node) {
    domainRepository.save(node);
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  ByonNode buildDeletedNode(String id, String userId) throws UsageException {
    ByonNode foundNode = domainRepository.findByTenantAndId(userId, id);

    if(foundNode == null) {
      throw new UsageException(String.format("Cannot find node with id: %s and userId: %s", id, userId));
    }

    ByonOperations.isDeletable(foundNode);

    return ByonNodeBuilder.of(foundNode)
        .allocated(false).build();
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeDeletedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
