package org.cloudiator.iaas.node;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeToNodeConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodeCandidateType;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.messaging.NodeCandidateMessageRepository;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Byon;
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

    //consistency check
    final NodeCandidate nodeCandidate = retrieveCandidate(pending);

    if(nodeCandidate == null) {
      throw new NodeSchedulingException(String.format("Cannot schedule byon with id: %s, as no"
          + " nodecandidate is associated with it.", pending.id()));
    }

    ByonNode byonNode = ByonNodeToNodeConverter.INSTANCE.applyBack(pending);

    ByonNodeAllocateRequestMessage byonNodeAllocateRequestMessage  = ByonNodeAllocateRequestMessage
        .newBuilder().setByonNode(ByonToByonMessageConverter.INSTANCE.apply(byonNode)).build();

    byonService.createByonPersistAllocAsync(byonNodeAllocateRequestMessage, byonFuture);

    try {
      byonFuture.get();
      byonNode.setAllocated(true);
      return ByonNodeToNodeConverter.INSTANCE.apply(byonNode);
    } catch (InterruptedException e) {
      throw new IllegalStateException("Interrupted while registering function", e);
    } catch (ExecutionException e) {
      throw new NodeSchedulingException(String.format("Could not schedule node %s.", pending), e);
    }
  }
}
