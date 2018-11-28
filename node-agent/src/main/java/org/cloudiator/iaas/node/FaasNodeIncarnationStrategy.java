package org.cloudiator.iaas.node;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.github.cloudiator.domain.*;
import org.cloudiator.messages.Function.CreateFunctionRequestMessage;
import org.cloudiator.messages.Function.FunctionCreatedResponse;
import org.cloudiator.messages.entities.FaasEntities.Function;
import org.cloudiator.messages.entities.FaasEntities.FunctionRequest;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.FunctionService;

import java.util.concurrent.ExecutionException;

public class FaasNodeIncarnationStrategy implements NodeCandidateIncarnationStrategy {

  public static class FaasNodeIncarnationFactory implements
      NodeCandidateIncarnationFactory {

    private final FunctionService functionService;

    @Inject
    public FaasNodeIncarnationFactory(FunctionService functionService) {
      this.functionService = functionService;
    }

    @Override
    public boolean canIncarnate(NodeCandidate nodeCandidate) {
      return NodeCandidateType.FAAS.equals(nodeCandidate.type());
    }

    @Override
    public NodeCandidateIncarnationStrategy create(String groupName, String userId) {
      return new FaasNodeIncarnationStrategy(groupName, userId, functionService);
    }
  }

  private final String groupName;
  private final String userId;
  private final FunctionService functionService;

  private FaasNodeIncarnationStrategy(String groupName,
      String userId, FunctionService functionService) {
    this.groupName = groupName;
    this.userId = userId;
    this.functionService = functionService;
  }

  @Override
  public Node apply(NodeCandidate nodeCandidate) throws ExecutionException {
    final SettableFutureResponseCallback<FunctionCreatedResponse, Function> callback =
        SettableFutureResponseCallback.create(FunctionCreatedResponse::getFunction);

    final FunctionRequest functionRequest = generateRequest(nodeCandidate);
    CreateFunctionRequestMessage createFunctionRequestMessage = CreateFunctionRequestMessage
        .newBuilder().setUserId(userId).setFunctionRequest(functionRequest).build();

    functionService.createFuntionAsync(createFunctionRequestMessage, callback);

    try {
      final Function function = callback.get();

      NodeProperties properties = NodePropertiesBuilder.newBuilder()
          .providerId(nodeCandidate.cloud().id())
          .memory(function.getMemory())
          .build();

      String name = String.format("%s-faas-%s",
          nodeCandidate.cloud().api().providerName(), function.getMemory());

      return NodeBuilder.newBuilder()
          .id(function.getId())
          .name(name)
          .nodeType(NodeType.FAAS)
          .nodeProperties(properties)
          .ipAddresses(ImmutableSet.of())
          .build();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Interrupted while registering function", e);
    }
  }

  private FunctionRequest generateRequest(NodeCandidate nodeCandidate) {
    return FunctionRequest.newBuilder()
        .setCloudId(nodeCandidate.cloud().id())
        .setLocationId(nodeCandidate.location().id())
        .setMemory((int) nodeCandidate.hardware().mbRam())
        .build();
  }


}
