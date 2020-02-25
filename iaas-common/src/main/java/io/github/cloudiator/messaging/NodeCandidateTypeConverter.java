package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.NodeCandidateType;
import javax.annotation.Nullable;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class NodeCandidateTypeConverter implements
    TwoWayConverter<MatchmakingEntities.NodeCandidateType, NodeCandidateType> {

  public static final NodeCandidateTypeConverter INSTANCE = new NodeCandidateTypeConverter();

  @Nullable
  @Override
  public NodeCandidateType apply(MatchmakingEntities.NodeCandidateType nodeCandidateType) {
    switch (nodeCandidateType) {
      case NC_IAAS:
        return NodeCandidateType.IAAS;
      case NC_FAAS:
        return NodeCandidateType.FAAS;
      case NC_PAAS:
        return NodeCandidateType.PAAS;
      case NC_BYON:
        return NodeCandidateType.BYON;
      case NC_SIMULATION:
        return NodeCandidateType.SIMULATION;
      case UNRECOGNIZED:
      default:
        throw new AssertionError("Unsupported node candidate type " + nodeCandidateType);
    }
  }

  @Override
  public MatchmakingEntities.NodeCandidateType applyBack(NodeCandidateType nodeCandidateType) {
    switch (nodeCandidateType) {
      case FAAS:
        return MatchmakingEntities.NodeCandidateType.NC_FAAS;
      case BYON:
        return MatchmakingEntities.NodeCandidateType.NC_BYON;
      case IAAS:
        return MatchmakingEntities.NodeCandidateType.NC_IAAS;
      case PAAS:
        return MatchmakingEntities.NodeCandidateType.NC_PAAS;
      case SIMULATION:
        return MatchmakingEntities.NodeCandidateType.NC_SIMULATION;
      default:
        throw new AssertionError("Illegal node candidate type " + nodeCandidateType);
    }
  }
}
