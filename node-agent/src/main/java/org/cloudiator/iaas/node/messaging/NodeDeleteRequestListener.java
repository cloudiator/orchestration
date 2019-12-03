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

package org.cloudiator.iaas.node.messaging;

import static org.cloudiator.iaas.node.config.Constants.NODE_EXECUTION_SERVICE_NAME;

import com.google.common.base.MoreObjects;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.persistance.NodeDomainRepository;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import org.cloudiator.iaas.node.messaging.DeleteNodeWorker.DeleteNodeRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDeleteRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeDeleteRequestListener.class);
  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private final DeleteNodeRequestWorkerFactory deleteNodeRequestWorkerFactory;
  private final ExecutorService nodeExecutorService;

  @Inject
  public NodeDeleteRequestListener(MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository,
      DeleteNodeRequestWorkerFactory deleteNodeRequestWorkerFactory,
      @Named(NODE_EXECUTION_SERVICE_NAME) ExecutorService nodeExecutorService) {
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
    this.deleteNodeRequestWorkerFactory = deleteNodeRequestWorkerFactory;
    this.nodeExecutorService = nodeExecutorService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeDeleteMessage.class, NodeDeleteMessage.parser(),
        new MessageCallback<NodeDeleteMessage>() {
          @Override
          public void accept(String id, NodeDeleteMessage content) {

            try {

              final String userId = content.getUserId();
              final String nodeId = content.getNodeId();

              LOGGER.debug(String
                  .format("%s is receiving request to delete node %s from user %s.", this, nodeId,
                      userId));

              Node node = nodeDomainRepository.findByTenantAndId(userId, nodeId);

              if (node == null) {
                LOGGER
                    .warn(String
                        .format("%s can not delete node with id %s as it does not exist.", this,
                            nodeId));
                messageInterface.reply(NodeDeleteResponseMessage.class, id,
                    Error.newBuilder().setCode(404)
                        .setMessage(String.format("Node with id %s does not exist.", nodeId))
                        .build());
                return;
              }

              LOGGER.info(String.format("%s is requests deletion of node %s.", this, node));

              final DeleteNodeWorker deleteNodeWorker = deleteNodeRequestWorkerFactory
                  .create(DeleteNodeRequest.of(id, node));

              nodeExecutorService.submit(deleteNodeWorker);

            } catch (Exception e) {
              LOGGER.error("Unexpected error while deleting node:" + e.getMessage(), e);
              messageInterface.reply(NodeDeleteResponseMessage.class, id,
                  Error.newBuilder().setCode(500)
                      .setMessage("Unexpected error while deleting node:" + e.getMessage())
                      .build());
            }


          }
        });
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
