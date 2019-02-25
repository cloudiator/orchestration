package org.cloudiator.iaas.node;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.OperatingSystems;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.NodeCandidateMessageRepository;
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

public class FaasNodeSchedulingStrategy implements NodeSchedulingStrategy {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FaasNodeSchedulingStrategy.class);

  private final FunctionService functionService;
  private final NodeCandidateMessageRepository nodeCandidateMessageRepository;

  @Inject
  public FaasNodeSchedulingStrategy(FunctionService functionService,
      NodeCandidateMessageRepository nodeCandidateMessageRepository) {
    this.functionService = functionService;
    this.nodeCandidateMessageRepository = nodeCandidateMessageRepository;
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

  private NodeCandidate retrieveCandidate(Node pending) {
    checkState(pending.nodeCandidate().isPresent(), "nodeCandidate is not present in pending node");

    final NodeCandidate nodeCandidate = nodeCandidateMessageRepository
        .getById(pending.userId(), pending.nodeCandidate().get());

    checkNotNull(nodeCandidate, String
        .format("NodeCandidate with id %s does no (longer) exist.", pending.nodeCandidate().get()));

    return nodeCandidate;
  }

  @Override
  public boolean canSchedule(Node pending) {
    return false;
  }

  @Override
  public Node schedule(Node pending) throws NodeSchedulingException {
    final SettableFutureResponseCallback<FunctionCreatedResponse, Function> callback =
        SettableFutureResponseCallback.create(FunctionCreatedResponse::getFunction);

    final NodeCandidate nodeCandidate = retrieveCandidate(pending);

    final FunctionRequest functionRequest = generateRequest(nodeCandidate);
    CreateFunctionRequestMessage createFunctionRequestMessage = CreateFunctionRequestMessage
        .newBuilder().setUserId(pending.userId()).setFunctionRequest(functionRequest).build();

    functionService.createFuntionAsync(createFunctionRequestMessage, callback);

    try {
      final Function function = callback.get();

      NodeProperties properties = NodePropertiesBuilder.newBuilder()
          .providerId(nodeCandidate.cloud().id())
          .memory((long) function.getMemory())
          .os(OperatingSystems.unknown())
          .build();

      return NodeBuilder.newBuilder()
          .generateId()
          .originId(function.getId())
          .userId(pending.userId())
          .state(NodeState.RUNNING)
          .name(pending.name())
          .nodeType(NodeType.FAAS)
          .nodeProperties(properties)
          .ipAddresses(ImmutableSet.of())
          .nodeCandidate(nodeCandidate.id())
          .build();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Interrupted while registering function", e);
    } catch (ExecutionException e) {
      throw new NodeSchedulingException(String.format("Could not schedule node %s.", pending), e);
    }
  }
}
