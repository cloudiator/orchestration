package io.github.cloudiator.iaas.common.messaging.converters;

import de.uniulm.omi.cloudiator.domain.Requirement;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.iaas.common.domain.NodeRequest;
import io.github.cloudiator.iaas.common.domain.NodeRequestImpl;
import org.cloudiator.messages.NodeEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class NodeRequirementsConverter
    implements OneWayConverter<NodeEntities.NodeRequirements, NodeRequest> {

    private final RequirementConverter requirementConverter = new RequirementConverter();

    @Nullable @Override
    public NodeRequest apply(@Nullable NodeEntities.NodeRequirements nodeRequest) {

        if (nodeRequest == null) {
            return null;
        }
        List<Requirement> requirementList =
            nodeRequest.getRequirementsList().stream().map(requirementConverter)
                .collect(Collectors.toList());
        return new NodeRequestImpl(requirementList);
    }
}
