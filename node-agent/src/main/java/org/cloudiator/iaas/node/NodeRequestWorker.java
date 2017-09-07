package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import io.github.cloudiator.iaas.common.messaging.RequirementConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.iaas.node.NodeRequestQueue.UserNodeRequest;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.Solution.OclSolutionRequest;
import org.cloudiator.messages.entities.Solution.OclSolutionResponse;
import org.cloudiator.messages.entities.SolutionEntities.OclProblem;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.SolutionService;
import org.cloudiator.messaging.services.SolutionServiceImpl;
import org.cloudiator.messaging.services.VirtualMachineService;

public class NodeRequestWorker implements Runnable {

  private final NodeRequestQueue nodeRequestQueue;
  private final SolutionService solutionService;
  private final VirtualMachineService virtualMachineService;
  private final RequirementConverter requirementConverter = new RequirementConverter();

  @Inject
  public NodeRequestWorker(NodeRequestQueue nodeRequestQueue,
      SolutionService solutionService,
      VirtualMachineService virtualMachineService) {
    this.nodeRequestQueue = nodeRequestQueue;
    this.solutionService = solutionService;
    this.virtualMachineService = virtualMachineService;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        final UserNodeRequest userNodeRequest = nodeRequestQueue.takeRequest();

        List<VirtualMachineCreatedResponse> responses = new ArrayList<>();

        ((SolutionServiceImpl) solutionService).setResponseTimeout(30000);
        final OclSolutionResponse oclSolutionResponse = solutionService
            .solveOCLProblem(createOclSolutionRequestFromNodeRequest(userNodeRequest));

        oclSolutionResponse.getSolution().getNodesList().forEach(
            virtualMachineRequest -> virtualMachineService.createVirtualMachineAsync(
                CreateVirtualMachineRequestMessage.newBuilder()
                    .setUserId(userNodeRequest.getUserId())
                    .setVirtualMachineRequest(virtualMachineRequest).build(),
                (content, error) -> responses.add(content)));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (ResponseException e) {
        //throw new IllegalStateException(e);
      } catch (Exception e) {

      }
    }
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
