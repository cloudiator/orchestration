package org.cloudiator.iaas.node;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodeCandidateType;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.CloudMessageRepository;
import io.github.cloudiator.messaging.RuntimeConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Function.CreateFunctionRequestMessage;
import org.cloudiator.messages.Function.FunctionCreatedResponse;
import org.cloudiator.messages.entities.FaasEntities.Function;
import org.cloudiator.messages.entities.FaasEntities.FunctionRequest;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.FunctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaasNodeIncarnationStrategy implements NodeCandidateIncarnationStrategy {

  public static class FaasNodeIncarnationFactory implements
      NodeCandidateIncarnationFactory {

    private final FunctionService functionService;
    private final CloudMessageRepository cloudMessageRepository;

    @Inject
    public FaasNodeIncarnationFactory(FunctionService functionService,
        CloudMessageRepository cloudMessageRepository) {
      this.functionService = functionService;
      this.cloudMessageRepository = cloudMessageRepository;
    }

    @Override
    public boolean canIncarnate(NodeCandidate nodeCandidate) {
      return NodeCandidateType.FAAS.equals(nodeCandidate.type());
    }

    @Override
    public NodeCandidateIncarnationStrategy create(String groupName, String userId) {
      return new FaasNodeIncarnationStrategy(groupName, userId,
          functionService, cloudMessageRepository);
    }
  }

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FaasNodeIncarnationFactory.class);

  private final String groupName;
  private final String userId;
  private final FunctionService functionService;
  private final CloudMessageRepository cloudMessageRepository;

  private FaasNodeIncarnationStrategy(String groupName, String userId,
      FunctionService functionService, CloudMessageRepository cloudMessageRepository) {
    this.groupName = groupName;
    this.userId = userId;
    this.functionService = functionService;
    this.cloudMessageRepository = cloudMessageRepository;
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
      final Cloud cloud = cloudMessageRepository.getById(userId, nodeCandidate.cloud().id());

      NodeProperties properties = NodePropertiesBuilder.newBuilder()
          .providerId(nodeCandidate.cloud().id())
          .memory((long) function.getMemory())
          .build();

      String name = String.format("%s-%s-%s-%s",
          cloud.api().providerName(),
          cloud.configuration().nodeGroup(),
          "faas",
          groupName);

      return NodeBuilder.newBuilder()
          .id(function.getId())
          .userId(userId)
          .state(NodeState.OK)
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
    RuntimeConverter runtimeConverter = RuntimeConverter.INSTANCE;
    return FunctionRequest.newBuilder()
        .setCloudId(nodeCandidate.cloud().id())
        .setLocationId(nodeCandidate.location().id())
        .setMemory((int) nodeCandidate.hardware().mbRam())
        .setRuntime(runtimeConverter.applyBack(nodeCandidate.environment().getRuntime()))
        .build();
  }
}
