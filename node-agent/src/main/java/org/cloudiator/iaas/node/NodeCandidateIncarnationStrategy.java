package org.cloudiator.iaas.node;


import de.uniulm.omi.cloudiator.util.function.ThrowingFunction;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;


public interface NodeCandidateIncarnationStrategy extends ThrowingFunction<NodeCandidate, Node> {

  interface NodeCandidateIncarnationFactory {

    boolean canIncarnate(NodeCandidate nodeCandidate);

    NodeCandidateIncarnationStrategy create(String groupName, String userId);
  }

}
