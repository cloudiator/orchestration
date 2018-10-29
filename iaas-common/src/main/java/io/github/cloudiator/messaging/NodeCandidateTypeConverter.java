package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.NodeCandidateType;
import org.cloudiator.messages.entities.MatchmakingEntities;

import javax.annotation.Nullable;

public class NodeCandidateTypeConverter implements
    OneWayConverter<MatchmakingEntities.NodeCandidateType, NodeCandidateType> {

  public static final NodeCandidateTypeConverter INSTANCE = new NodeCandidateTypeConverter();

  @Nullable
  @Override
  public NodeCandidateType apply(@Nullable MatchmakingEntities.NodeCandidateType nodeCandidateType) {
    switch (nodeCandidateType) {
      case NC_IAAS:
        return NodeCandidateType.IAAS;
      case NC_FAAS:
        return NodeCandidateType.FAAS;
      case NC_PAAS:
        return NodeCandidateType.PAAS;
      case NC_BYON:
        return NodeCandidateType.BYON;
      case UNRECOGNIZED:
      default:
        throw new IllegalStateException("Unsupported node candidate type " + nodeCandidateType);
    }
  }
}
