package org.cloudiator.iaas.node;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeToNodeConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodeCandidateType;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.messaging.NodeCandidateMessageRepository;
import io.github.cloudiator.messaging.NodePropertiesMessageToNodePropertiesConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Byon.ByonNodeAllocateRequestMessage;
import org.cloudiator.messages.Byon.ByonNodeAllocatedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.ByonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeSchedulingStrategy implements NodeSchedulingStrategy {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonNodeSchedulingStrategy.class);

  private final ByonService byonService;
  private final NodeCandidateMessageRepository nodeCandidateMessageRepository;
  private static final NodePropertiesMessageToNodePropertiesConverter NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();

  @Inject
  public ByonNodeSchedulingStrategy(ByonService byonService,
      NodeCandidateMessageRepository nodeCandidateMessageRepository) {
    this.byonService = byonService;
    this.nodeCandidateMessageRepository = nodeCandidateMessageRepository;
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
    return retrieveCandidate(pending).type().equals(NodeCandidateType.BYON);
  }

  @Override
  public Node schedule(Node pending) throws NodeSchedulingException {
    final SettableFutureResponseCallback<ByonNodeAllocatedResponse, ByonNodeAllocatedResponse>
        byonFuture = SettableFutureResponseCallback.create();

    final NodeCandidate nodeCandidate = retrieveCandidate(pending);

    if (nodeCandidate == null) {
      throw new NodeSchedulingException(String.format("Cannot schedule byon with id: %s, as no"
          + " nodecandidate is associated with it.", pending.id()));
    }

    //node is 'running' now
    ByonNodeAllocateRequestMessage byonNodeAllocateRequestMessage = ByonNodeAllocateRequestMessage
        .newBuilder().setUserId(pending.userId()).setProperties(
            NODE_PROPERTIES_CONVERTER.applyBack(buildProperties(nodeCandidate)))
        .setAllocated(true).build();

    byonService.createByonPersistAllocAsync(byonNodeAllocateRequestMessage, byonFuture);

    try {
      ByonNodeAllocatedResponse response = byonFuture.get();
      final ByonNode scheduledNode = ByonToByonMessageConverter.INSTANCE
          .applyBack(response.getNode());
      final Node byonNode = ByonNodeToNodeConverter.INSTANCE.apply(scheduledNode);

      return NodeBuilder.of(byonNode).id(pending.id()).generateName(pending.name())
          .nodeCandidate(nodeCandidate.id()).originId(byonNode.id()).nodeType(NodeType.BYON).build();

    } catch (InterruptedException e) {
      throw new IllegalStateException("Interrupted while registering function", e);
    } catch (ExecutionException e) {
      throw new NodeSchedulingException(String.format("Could not schedule node %s.", pending), e);
    }
  }

  // todo: figure out if providerId can be left out because "it smells" -> "Byon-Cloud"
  private NodeProperties buildProperties(NodeCandidate nodeCandidate) {
    return NodePropertiesBuilder.newBuilder()
        .providerId(nodeCandidate.cloud().id())
        .numberOfCores(nodeCandidate.hardware().numberOfCores())
        .memory(nodeCandidate.hardware().mbRam())
        .disk(nodeCandidate.hardware().gbDisk().orElse(null))
        .os(nodeCandidate.image().operatingSystem())
        .geoLocation(nodeCandidate.location().geoLocation().orElse(null))
        .build();
  }
}
