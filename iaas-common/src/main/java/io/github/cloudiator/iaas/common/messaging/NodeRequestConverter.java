package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.domain.Requirement;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.iaas.common.domain.NodeRequest;
import io.github.cloudiator.iaas.common.domain.NodeRequestImpl;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.NodeEntities;

public class NodeRequestConverter implements
    OneWayConverter<NodeEntities.NodeRequest, NodeRequest> {

  private final RequirementConverter requirementConverter = new RequirementConverter();

  @Nullable
  @Override
  public NodeRequest apply(@Nullable NodeEntities.NodeRequest nodeRequest) {

    if (nodeRequest == null) {
      return null;
    }
    List<Requirement> requirementList = nodeRequest.getRequirementsList().stream().map(
        requirementConverter).collect(Collectors.toList());
    return new NodeRequestImpl(requirementList);
  }
}
