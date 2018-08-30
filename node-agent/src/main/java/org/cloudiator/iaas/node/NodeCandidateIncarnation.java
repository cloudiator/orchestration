package org.cloudiator.iaas.node;


import de.uniulm.omi.cloudiator.util.function.ThrowingFunction;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;


public interface NodeCandidateIncarnation extends ThrowingFunction<NodeCandidate, Node> {

  interface NodeCandidateIncarnationFactory {

    boolean canIncarnate(NodeCandidate nodeCandidate);

    NodeCandidateIncarnation create(String groupName, String userId);
  }

}
