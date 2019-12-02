/*
 * Copyright (c) 2014-2019 University of Ulm
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.NodeCandidateMessageRepository;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.NodeDomainRepository;
import io.github.cloudiator.persistance.TransactionRetryer;
import org.cloudiator.iaas.node.NodeStateMachine;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messages.Node.NodeRequestResponse;

import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNodeWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CreateNodeWorker.class);

  private final CreateNodeRequest createNodeRequest;

  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeStateMachine nodeStateMachine;
  private final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private final NodeCandidateMessageRepository nodeCandidateMessageRepository;

  @Inject
  public CreateNodeWorker(@Assisted CreateNodeRequest createNodeRequest,
      MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository,
      NodeStateMachine nodeStateMachine,
      NodeCandidateMessageRepository nodeCandidateMessageRepository) {
    this.createNodeRequest = createNodeRequest;
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
    this.nodeStateMachine = nodeStateMachine;
    this.nodeCandidateMessageRepository = nodeCandidateMessageRepository;
  }

  @Transactional
  Node persistNode(Node node) {
    nodeDomainRepository.save(node);
    return node;
  }


  private NodeCandidate retrieveCandidate(String userId, String nodeCandidateId) {

    final NodeCandidate nodeCandidate = nodeCandidateMessageRepository
        .getById(userId, nodeCandidateId);

    checkNotNull(nodeCandidate, String
        .format("NodeCandidate with id %s does no (longer) exist.", nodeCandidateId));

    return nodeCandidate;
  }


  @Override
  public void run() {

    try {

      final String userId = createNodeRequest.getNodeRequestMessage().getUserId();
      final String groupName = createNodeRequest.getNodeRequestMessage().getGroupName();
      final NodeCandidate nodeCandidate = retrieveCandidate(userId,
          createNodeRequest.getNodeRequestMessage().getNodeCandidate());

      LOGGER.debug(
          String.format(
              "%s is starting nodes to fulfill node request %s.",
              this, createNodeRequest.getNodeRequestMessage()));

      //generate the pending node
      final Node pending = NodeBuilder.newBuilder().generateId().generateName(groupName)
          .nodeType(
              NodeType.UNKOWN)
          .nodeProperties(
              NodePropertiesBuilder.newBuilder()
                  .providerId(nodeCandidate.cloud().id())
                  .build()).state(NodeState.PENDING).nodeCandidate(nodeCandidate.id())
          .userId(userId).build();

      synchronized (CreateNodeWorker.class) {
        TransactionRetryer.retry(() -> persistNode(pending));
      }

      final Node running = nodeStateMachine
          .apply(pending, NodeState.RUNNING, new Object[]{nodeCandidate});

      LOGGER.debug(
          String.format("%s is replying success for request %s with node %s.", this,
              createNodeRequest.getNodeRequestMessage(), running));
      messageInterface.reply(createNodeRequest.getId(),
          NodeRequestResponse.newBuilder()
              .setNode(NODE_CONVERTER.apply(running)).build());
    } catch (Exception e) {
      LOGGER.error(String
          .format("Unexpected error %s occurred while working on request %s.", e.getMessage(),
              createNodeRequest.getNodeRequestMessage()), e);
      messageInterface.reply(NodeRequestResponse.class, createNodeRequest.getId(),
          Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
    }

  }

  public static class CreateNodeRequest {

    private final String id;
    private final NodeRequestMessage nodeRequestMessage;

    private CreateNodeRequest(String id,
        NodeRequestMessage nodeRequestMessage) {
      this.id = id;
      this.nodeRequestMessage = nodeRequestMessage;
    }

    public static CreateNodeRequest of(String id, NodeRequestMessage nodeRequestMessage) {
      return new CreateNodeRequest(id, nodeRequestMessage);
    }

    public String getId() {
      return id;
    }

    public NodeRequestMessage getNodeRequestMessage() {
      return nodeRequestMessage;
    }
  }
}
