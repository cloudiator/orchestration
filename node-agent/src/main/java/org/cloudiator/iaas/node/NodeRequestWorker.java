package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.iaas.common.messaging.converters.NodeToNodeMessageConverter;
import io.github.cloudiator.iaas.common.messaging.converters.RequirementConverter;
import io.github.cloudiator.iaas.common.messaging.converters.VirtualMachineMessageToVirtualMachine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.cloudiator.iaas.node.NodeRequestQueue.UserNodeRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.Solution.OclSolutionRequest;
import org.cloudiator.messages.entities.Solution.OclSolutionResponse;
import org.cloudiator.messages.entities.SolutionEntities.OclProblem;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.SolutionService;
import org.cloudiator.messaging.services.VirtualMachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRequestWorker implements Runnable {

  private final NodeRequestQueue nodeRequestQueue;
  private final SolutionService solutionService;
  private final VirtualMachineService virtualMachineService;
  private final RequirementConverter requirementConverter = new RequirementConverter();
  private final MessageInterface messageInterface;
  private final VirtualMachineToNode virtualMachineToNode = new VirtualMachineToNode();
  private final VirtualMachineMessageToVirtualMachine virtualMachineConverter = new VirtualMachineMessageToVirtualMachine();
  private final NodeToNodeMessageConverter nodeConverter = new NodeToNodeMessageConverter();
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestWorker.class);

  @Inject
  public NodeRequestWorker(NodeRequestQueue nodeRequestQueue,
      SolutionService solutionService,
      VirtualMachineService virtualMachineService,
      MessageInterface messageInterface) {
    this.nodeRequestQueue = nodeRequestQueue;
    this.solutionService = solutionService;
    this.virtualMachineService = virtualMachineService;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {

      UserNodeRequest userNodeRequest = null;
      try {
        userNodeRequest = nodeRequestQueue.takeRequest();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

      try {
        final String userId = userNodeRequest.getUserId();
        final String messageId = userNodeRequest.getMessageId();

        List<VirtualMachine> responses = new ArrayList<>();
        List<Error> errors = new ArrayList<>();

        final OclSolutionResponse oclSolutionResponse = solutionService
            .solveOCLProblem(createOclSolutionRequestFromNodeRequest(userNodeRequest));

        CountDownLatch countDownLatch = new CountDownLatch(
            oclSolutionResponse.getSolution().getNodesCount());

        oclSolutionResponse.getSolution().getNodesList().forEach(
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
        messageInterface.reply(NodeRequestResponse.class, userNodeRequest.getMessageId(),
            Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
      }
    }
  }

  private static String buildErrorMessage(List<Error> errors) {
    final String errorFormat = "%s Error(s) occurred during provisioning of nodes: %s";
    return String.format(errorFormat, errors.size(),
        errors.stream().map(error -> error.getCode() + " " + error.getMessage())
            .collect(Collectors.toList()));
  }

  private OclSolutionRequest createOclSolutionRequestFromNodeRequest(
      UserNodeRequest userNodeRequest) {

    List<CommonEntities.OclRequirement> oclRequirements = userNodeRequest.getNodeRequest()
        .requirements().stream().map(requirement -> {
          if (requirement instanceof de.uniulm.omi.cloudiator.domain.OclRequirement) {
            return requirementConverter.applyBack(requirement).getOclRequirement();
          } else {
            throw new IllegalStateException("Currently only ocl requirements are supported.");
          }
        }).collect(Collectors.toList());

    OclProblem oclProblem = OclProblem.newBuilder().addAllRequirements(oclRequirements).build();
    return OclSolutionRequest.newBuilder().setUserId(userNodeRequest.getUserId())
        .setProblem(oclProblem).build();
  }
}
