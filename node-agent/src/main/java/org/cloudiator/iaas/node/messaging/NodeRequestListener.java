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

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.NodeDomainRepository;
import org.cloudiator.iaas.node.NodeStateMachine;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.entities.MatchmakingEntities.NodeCandidate;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestListener.class);
  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeStateMachine nodeStateMachine;
  private final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public NodeRequestListener(MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository,
      NodeStateMachine nodeStateMachine) {
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
    this.nodeStateMachine = nodeStateMachine;
  }

  @Transactional
  Node persistNode(Node node) {
    nodeDomainRepository.save(node);
    return node;
  }


  @Override
  public void run() {
    messageInterface.subscribe(NodeRequestMessage.class, NodeRequestMessage.parser(),
        (id, content) -> {
          LOGGER.info(String.format("Receiving new node request %s. ", content));

          try {

            final String userId = content.getUserId();
            final String groupName = content.getGroupName();
            final NodeCandidate nodeCandidate = content.getNodeCandidate();

            LOGGER.debug(
                String.format(
                    "%s is starting nodes to fulfill node request %s.",
                    this, content));

            //generate the pending node
            final Node pending = NodeBuilder.newBuilder().generateId().generateName(groupName)
                .nodeType(
                    NodeType.UNKOWN)
                .nodeProperties(
                    NodePropertiesBuilder.newBuilder()
                        .providerId(nodeCandidate.getCloud().getId())
                        .build()).state(NodeState.PENDING).nodeCandidate(nodeCandidate.getId())
                .userId(userId).build();

            persistNode(pending);

            final Node running = nodeStateMachine
                .apply(pending, NodeState.RUNNING, new Object[]{nodeCandidate});

            LOGGER.debug(
                String.format("%s is replying success for request %s with node %s.", this,
                    content, running));
            messageInterface.reply(id,
                NodeRequestResponse.newBuilder()
                    .setNode(NODE_CONVERTER.apply(running)).build());
          } catch (Exception e) {
            LOGGER.error(String
                .format("Unexpected error %s occurred while working on request %s.", e.getMessage(),
                    content), e);
            messageInterface.reply(NodeRequestResponse.class, id,
                Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
          }


        });
  }
}
