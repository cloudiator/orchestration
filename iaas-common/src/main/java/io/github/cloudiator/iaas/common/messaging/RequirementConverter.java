package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.domain.IdRequirement;
import de.uniulm.omi.cloudiator.domain.OclRequirement;
import de.uniulm.omi.cloudiator.domain.Requirement;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.CommonEntities;

public class RequirementConverter implements
    TwoWayConverter<CommonEntities.Requirement, Requirement> {

  @Override
  public CommonEntities.Requirement applyBack(Requirement requirement) {

    if (requirement instanceof IdRequirement) {
      return CommonEntities.Requirement.newBuilder().setIdRequirement(
          CommonEntities.IdRequirement.newBuilder()
              .setHardwareId(((IdRequirement) requirement).getHardwareId())
              .setLocationId(((IdRequirement) requirement).getLocationId())
              .setImageId(((IdRequirement) requirement).getImageId())
              .build()).build();
    } else if (requirement instanceof OclRequirement) {
      return CommonEntities.Requirement.newBuilder().setOclRequirement(
          CommonEntities.OclRequirement.newBuilder()
              .setConstraint(((OclRequirement) requirement).constraint()).build()).build();
    } else {
      throw new AssertionError(
          String.format("Unknown requirement type %s", requirement.getClass()));
    }
  }

  @Override
  public Requirement apply(CommonEntities.Requirement requirement) {
    switch (requirement.getRequirementCase()) {
      case IDREQUIREMENT:
        return new IdRequirement(requirement.getIdRequirement().getHardwareId(),
            requirement.getIdRequirement().getLocationId(),
            requirement.getIdRequirement().getImageId());
      case OCLREQUIREMENT:
        return new OclRequirement(requirement.getOclRequirement().getConstraint());
      case REQUIREMENT_NOT_SET:
        throw new IllegalStateException("Requirement not set");
      default:
        throw new AssertionError(
            String.format("Unknown requirement case %s", requirement.getRequirementCase()));
    }
  }
}
