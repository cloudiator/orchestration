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

package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.domain.NodeGroups;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.messaging.NodeCandidateConverter;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import io.github.cloudiator.persistance.NodeDomainRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.cloudiator.iaas.node.NodeCandidateIncarnationStrategy.NodeCandidateIncarnationFactory;
import org.cloudiator.iaas.node.NodeRequestQueue.NodeRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.MatchmakingEntities.NodeCandidate;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.MatchmakingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRequestWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestWorker.class);
  private static final NodeGroupMessageToNodeGroup NODE_GROUP_CONVERTER = new NodeGroupMessageToNodeGroup();
  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = NodeCandidateConverter.INSTANCE;
  private final NodeRequestQueue nodeRequestQueue;
  private final MatchmakingService matchmakingService;
  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeCandidateIncarnationFactory nodeCandidateIncarnationFactory;
  private final NodeStateMachine nodeStateMachine;

  @Inject
  public NodeRequestWorker(NodeRequestQueue nodeRequestQueue,
      MatchmakingService matchmakingService,
      MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository,
      NodeCandidateIncarnationFactory nodeCandidateIncarnationFactory,
      NodeStateMachine nodeStateMachine) {
    this.nodeRequestQueue = nodeRequestQueue;
    this.matchmakingService = matchmakingService;
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
    this.nodeCandidateIncarnationFactory = nodeCandidateIncarnationFactory;
    this.nodeStateMachine = nodeStateMachine;
  }

  private static String buildErrorMessage(List<Throwable> exceptions) {
    final String errorFormat = "%s Error(s) occurred during provisioning of nodes: %s";
    return String.format(errorFormat, exceptions.size(),
        exceptions.stream().map(Throwable::getMessage)
            .collect(Collectors.joining("/n")));
  }

  @Transactional
  void persistNodeGroup(NodeGroup nodeGroup) {
    nodeDomainRepository.save(nodeGroup);
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {

      NodeRequest userNodeRequest = null;
      try {
        userNodeRequest = nodeRequestQueue.takeRequest();
        LOGGER.info(String.format("%s is now handling node request %s.", this, userNodeRequest));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

      try {
        final String userId = userNodeRequest.getNodeRequestMessage().getUserId();
        final String messageId = userNodeRequest.getId();
        final String groupName = userNodeRequest.getNodeRequestMessage().getGroupName();

        List<NodeCandidate> nodeCandidates;
        switch (userNodeRequest.getNodeRequestMessage().getRequestCase()) {
          case REQUIREMENTS:
            nodeCandidates = matchmaking(userNodeRequest, userId);
            break;
          case NODECANDIDATE:
            nodeCandidates = Collections
                .singletonList(userNodeRequest.getNodeRequestMessage().getNodeCandidate());
            break;
          case REQUEST_NOT_SET:
          default:
            throw new AssertionError(
                "Illegal request case " + userNodeRequest.getNodeRequestMessage().getRequestCase());
        }

        Set<Node> nodes = new HashSet<>();

        CountDownLatch countDownLatch = new CountDownLatch(
            nodeCandidates.size());

        LOGGER.debug(
            String.format(
                "%s is starting to start virtual machines to fulfill node request %s. Number of nodes is %s.",
                this, userNodeRequest, nodeCandidates.size()));

        nodeCandidates.parallelStream().forEach(
            nodeCandidate -> {

              //incarnate node candidate to a new
              Node incarnation = nodeCandidateIncarnationFactory.create(groupName, userId)
                  .apply(NODE_CANDIDATE_CONVERTER.apply(nodeCandidate));

              if (incarnation.state().equals(NodeState.FAILED)) {
                incarnation = nodeStateMachine.fail(incarnation, new Object[0], null);
              } else {
                try {
                  incarnation = nodeStateMachine.apply(incarnation, NodeState.RUNNING, null);
                } catch (ExecutionException e) {
                  throw new IllegalStateException("Could not switch state of node");
                }
              }

              nodes.add(incarnation);

              countDownLatch.countDown();
            });

        //todo: add timeout?
        LOGGER.debug(String
            .format("%s is waiting for all virtual machines to start for node request %s.", this,
                userNodeRequest));
        countDownLatch.await();
        LOGGER.debug(String
            .format("%s finished waiting for all virtual machines to start for node request %s.",
                this,
                userNodeRequest));

        //create node group
        final NodeGroup nodeGroup = createNodeGroup(userId, nodes);

        LOGGER.debug(
            String.format("%s is replying success for request %s with node group %s.", this,
                userNodeRequest, nodeGroup));
        messageInterface.reply(messageId,
            NodeRequestResponse.newBuilder()
                .setNodeGroup(NODE_GROUP_CONVERTER.applyBack(nodeGroup)).build());
      } catch (Exception e) {
        LOGGER.error(String
            .format("Unexpected error %s occurred while working on request %s.", e.getMessage(),
                userNodeRequest), e);
        messageInterface.reply(NodeRequestResponse.class, userNodeRequest.getId(),
            Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
      }
    }
  }

  private NodeGroup createNodeGroup(String userId, Set<Node> nodes) {
    final NodeGroup nodeGroup = NodeGroups
        .of(userId, nodes);
    LOGGER
        .debug(String.format("%s is grouping the nodes %s to node group %s.", this,
            nodes, nodeGroup));

    //persist the node group
    persistNodeGroup(nodeGroup);

    return nodeGroup;
  }

  private List<NodeCandidate> matchmaking(NodeRequest userNodeRequest, String userId)
      throws ResponseException {
    LOGGER.debug(String
        .format("%s is calling matchmaking engine to derive configuration for request %s.",
            this, userNodeRequest));

    final MatchmakingResponse matchmakingResponse = matchmakingService.requestMatch(
        MatchmakingRequest.newBuilder()
            .setNodeRequirements(userNodeRequest.getNodeRequestMessage().getRequirements())
            .setUserId(userId)
            .build());

    LOGGER.debug(String
        .format("%s received matchmaking response for node request %s. Selected offer is %s.",
            this, userNodeRequest, matchmakingResponse));

    return matchmakingResponse.getCandidatesList();
  }

  @Override
  public String toString() {
    return "NodeRequestWorker{}";
  }
}
