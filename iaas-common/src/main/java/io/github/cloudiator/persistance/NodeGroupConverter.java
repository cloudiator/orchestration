package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.domain.NodeGroups;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class NodeGroupConverter implements OneWayConverter<NodeGroupModel, NodeGroup> {

  private final NodeConverter nodeConverter = new NodeConverter();

  @Nullable
  @Override
  public NodeGroup apply(@Nullable NodeGroupModel nodeGroupModel) {
    if (nodeGroupModel == null) {
      return null;
    }

    return NodeGroups.of(nodeGroupModel.getDomainId(),
        nodeGroupModel.getNodes().stream().map(nodeConverter).collect(
            Collectors.toList()));
  }
}
