package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.domain.NodeGroups;
import io.github.cloudiator.messaging.NodeCandidateConverter;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import io.github.cloudiator.persistance.NodeDomainRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.cloudiator.iaas.node.NodeCandidateIncarnation.NodeCandidateIncarnationFactory;
import org.cloudiator.iaas.node.NodeRequestQueue.NodeRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.MatchmakingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRequestWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestWorker.class);
  private final NodeRequestQueue nodeRequestQueue;
  private final MatchmakingService matchmakingService;
  private final MessageInterface messageInterface;
  private static final NodeGroupMessageToNodeGroup NODE_GROUP_CONVERTER = new NodeGroupMessageToNodeGroup();
  private final NodeDomainRepository nodeDomainRepository;
  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = NodeCandidateConverter.INSTANCE;
  private final NodeCandidateIncarnationFactory nodeCandidateIncarnationFactory;

  @Inject
  public NodeRequestWorker(NodeRequestQueue nodeRequestQueue,
      MatchmakingService matchmakingService,
      MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository,
      NodeCandidateIncarnationFactory nodeCandidateIncarnationFactory) {
    this.nodeRequestQueue = nodeRequestQueue;
    this.matchmakingService = matchmakingService;
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
    this.nodeCandidateIncarnationFactory = nodeCandidateIncarnationFactory;

  }

  private static String buildErrorMessage(List<Throwable> exceptions) {
    final String errorFormat = "%s Error(s) occurred during provisioning of nodes: %s";
    return String.format(errorFormat, exceptions.size(),
        exceptions.stream().map(Throwable::getMessage)
            .collect(Collectors.toList()));
  }

  @Transactional
  void persistNodeGroup(NodeGroup nodeGroup, String userId) {
    nodeDomainRepository.save(nodeGroup, userId);
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

        List<Node> nodes = new ArrayList<>();
        List<Throwable> exceptions = new ArrayList<>();

        LOGGER.debug(String
            .format("%s is calling matchmaking engine to derive configuration for request %s.",
                this, userNodeRequest));

        final MatchmakingResponse matchmakingResponse = matchmakingService.requestMatch(
            MatchmakingRequest.newBuilder()
                .setNodeRequirements(userNodeRequest.getNodeRequestMessage().getNodeRequest())
                .setUserId(userId)
                .build());

        LOGGER.debug(String
            .format("%s received matchmaking response for node request %s. Selected offer is %s.",
                this, userNodeRequest, matchmakingResponse));

        CountDownLatch countDownLatch = new CountDownLatch(
            matchmakingResponse.getCandidatesCount());

        LOGGER.debug(
            String.format(
                "%s is starting to start virtual machines to fulfill node request %s. Number of nodes is %s.",
                this, userNodeRequest, matchmakingResponse.getCandidatesCount()));

        matchmakingResponse.getCandidatesList().parallelStream().forEach(
            nodeCandidate -> {

              //incarnate node candidate to a new
              try {
                final Node incarnation = nodeCandidateIncarnationFactory.create(groupName, userId)
                    .apply(NODE_CANDIDATE_CONVERTER.apply(nodeCandidate));
                nodes.add(incarnation);
              } catch (ExecutionException e) {
                exceptions.add(e.getCause());
              }
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

        if (exceptions.isEmpty()) {

          //create node group
          final NodeGroup nodeGroup = NodeGroups
              .of(nodes);
          LOGGER
              .debug(String.format("%s is grouping the nodes of request %s to node group %s.", this,
                  userNodeRequest, nodeGroup));

          //persist the node group
          persistNodeGroup(nodeGroup, userId);

          LOGGER.debug(
              String.format("%s is replying success for request %s with node group %s.", this,
                  userNodeRequest, nodeGroup));
          messageInterface.reply(messageId,
              NodeRequestResponse.newBuilder()
                  .setNodeGroup(NODE_GROUP_CONVERTER.applyBack(nodeGroup)).build());

        } else {
          LOGGER.error(String.format(
              "%s received error messages %s while starting virtual machines for node request %s. Replying with failure.",
              this, buildErrorMessage(exceptions), userNodeRequest));
          messageInterface.reply(NodeRequestResponse.class, messageId,
              Error.newBuilder().setMessage(buildErrorMessage(exceptions)).setCode(500).build());
        }
      } catch (Exception e) {
        LOGGER.error(String
            .format("Unexpected error %s occurred while working on request %s.", e.getMessage(),
                userNodeRequest), e);
        messageInterface.reply(NodeRequestResponse.class, userNodeRequest.getId(),
            Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
      }
    }
  }

  @Override
  public String toString() {
    return "NodeRequestWorker{}";
  }
}
