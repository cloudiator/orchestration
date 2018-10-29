package org.cloudiator.iaas.node;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CompositeNodeCandidateIncarnationStrategy implements NodeCandidateIncarnationStrategy {

  public static class CompositeNodeCandidateIncarnationFactory implements
      NodeCandidateIncarnationFactory {

    private final Set<NodeCandidateIncarnationFactory> factories;

    @Inject
    public CompositeNodeCandidateIncarnationFactory(
        Set<NodeCandidateIncarnationFactory> factories) {
      this.factories = factories;
    }


    @Override
    public boolean canIncarnate(NodeCandidate nodeCandidate) {
      //todo: check
      return true;
    }

    @Override
    public NodeCandidateIncarnationStrategy create(String groupName, String userId) {
      return new CompositeNodeCandidateIncarnationStrategy(groupName, userId, factories);
    }
  }

  private final Set<NodeCandidateIncarnationFactory> factories;
  private final String groupName;
  private final String userId;

  public CompositeNodeCandidateIncarnationStrategy(String groupName, String userId,
      Set<NodeCandidateIncarnationFactory> factories) {
    this.groupName = groupName;
    this.userId = userId;
    this.factories = factories;
  }

  @Override
  public Node apply(NodeCandidate nodeCandidate) throws ExecutionException {

    for (NodeCandidateIncarnationFactory factory : factories) {
      if (factory.canIncarnate(nodeCandidate)) {
        return factory.create(groupName, userId).apply(nodeCandidate);
      }
    }

    throw new AssertionError(
        String.format("None of the factories (%s) supports the node candidate %s.",
            Joiner.on(",").join(factories), nodeCandidate));
  }
}
