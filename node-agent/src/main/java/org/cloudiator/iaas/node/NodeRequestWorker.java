package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.iaas.common.messaging.converters.NodeToNodeMessageConverter;
import io.github.cloudiator.iaas.common.messaging.converters.VirtualMachineMessageToVirtualMachine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.cloudiator.iaas.node.NodeRequestQueue.NodeRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.MatchmakingService;
import org.cloudiator.messaging.services.VirtualMachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRequestWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestWorker.class);
  private final NodeRequestQueue nodeRequestQueue;
  private final MatchmakingService matchmakingService;
  private final VirtualMachineService virtualMachineService;
  private final MessageInterface messageInterface;
  private final VirtualMachineToNode virtualMachineToNode = new VirtualMachineToNode();
  private final VirtualMachineMessageToVirtualMachine virtualMachineConverter = new VirtualMachineMessageToVirtualMachine();
  private final NodeToNodeMessageConverter nodeConverter = new NodeToNodeMessageConverter();

  @Inject
  public NodeRequestWorker(NodeRequestQueue nodeRequestQueue,
      MatchmakingService matchmakingService,
      VirtualMachineService virtualMachineService,
      MessageInterface messageInterface) {
    this.nodeRequestQueue = nodeRequestQueue;
    this.matchmakingService = matchmakingService;
    this.virtualMachineService = virtualMachineService;
    this.messageInterface = messageInterface;
  }

  private static String buildErrorMessage(List<Error> errors) {
    final String errorFormat = "%s Error(s) occurred during provisioning of nodes: %s";
    return String.format(errorFormat, errors.size(),
        errors.stream().map(error -> error.getCode() + " " + error.getMessage())
            .collect(Collectors.toList()));
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {

      NodeRequest userNodeRequest = null;
      try {
        userNodeRequest = nodeRequestQueue.takeRequest();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

      try {
        final String userId = userNodeRequest.getNodeRequestMessage().getUserId();
        final String messageId = userNodeRequest.getId();

        List<VirtualMachine> responses = new ArrayList<>();
        List<Error> errors = new ArrayList<>();

        final MatchmakingResponse matchmakingResponse = matchmakingService.requestMatch(
            MatchmakingRequest.newBuilder()
                .setRequirements(userNodeRequest.getNodeRequestMessage().getNodeRequest())
                .setUserId(userId)
                .build());

        CountDownLatch countDownLatch = new CountDownLatch(
            matchmakingResponse.getNodesCount());

        matchmakingResponse.getNodesList().forEach(
            virtualMachineRequest -> virtualMachineService.createVirtualMachineAsync(
                CreateVirtualMachineRequestMessage.newBuilder()
                    .setUserId(userId)
                    .setVirtualMachineRequest(virtualMachineRequest).build(),
                (content, error) -> {
                  if (content != null) {
                    responses.add(virtualMachineConverter.apply(content.getVirtualMachine()));
                  } else if (error != null) {
                    errors.add(error);
                  } else {
                    throw new IllegalStateException(
                        "Neither content or error are set in response.");
                  }
                  countDownLatch.countDown();
                }));

        //add timeout?
        countDownLatch.await();

        if (errors.isEmpty()) {
          messageInterface.reply(messageId,
              NodeRequestResponse.newBuilder().addAllNode(responses.stream()
                  .map(virtualMachineToNode)
                  .map(nodeConverter)
                  .collect(Collectors.toList())).build());
        } else {
          messageInterface.reply(NodeRequestResponse.class, messageId,
              Error.newBuilder().setMessage(buildErrorMessage(errors)).setCode(500).build());
        }
      } catch (Exception e) {
        LOGGER.error(String.format("Error %s occurred while working on request %s.", e.getMessage(),
            userNodeRequest), e);
        messageInterface.reply(NodeRequestResponse.class, userNodeRequest.getId(),
            Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
      }
    }
  }
}
