package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemBuilder;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersions;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.CommonEntities;

/**
 * Created by daniel on 08.06.17.
 */
public class OperatingSystemConverter implements
    TwoWayConverter<CommonEntities.OperatingSystem, OperatingSystem> {

  @Override
  public CommonEntities.OperatingSystem applyBack(OperatingSystem operatingSystem) {
    return CommonEntities.OperatingSystem.newBuilder()
        .setOperatingSystemArchitecture(operatingSystem.operatingSystemArchitecture().name())
        .setOperatingSystemFamily(operatingSystem.operatingSystemFamily().name())
        .setOperatingSystemVersion(operatingSystem.operatingSystemFamily().name()).build();
  }

  @Override
  public OperatingSystem apply(CommonEntities.OperatingSystem operatingSystem) {
    return OperatingSystemBuilder.newBuilder().architecture(
        OperatingSystemArchitecture.valueOf(operatingSystem.getOperatingSystemArchitecture()))
        .family(
            OperatingSystemFamily.valueOf(operatingSystem.getOperatingSystemFamily())).version(
            OperatingSystemVersions.of(Integer.valueOf(operatingSystem.getOperatingSystemVersion()),
                operatingSystem.getOperatingSystemVersion())).build();
  }
}
