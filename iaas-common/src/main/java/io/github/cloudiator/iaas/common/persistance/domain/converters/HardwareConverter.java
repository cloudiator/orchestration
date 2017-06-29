package io.github.cloudiator.iaas.common.persistance.domain.converters;

import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavorBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareModel;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
public class HardwareConverter implements OneWayConverter<HardwareModel, HardwareFlavor> {

  private final LocationConverter locationConverter = new LocationConverter();

  @Nullable
  @Override
  public HardwareFlavor apply(@Nullable HardwareModel hardwareModel) {
    if (hardwareModel == null) {
      return null;
    }
    return HardwareFlavorBuilder.newBuilder().name(hardwareModel.getName())
        .providerId(hardwareModel.getProviderId()).id(hardwareModel.getCloudUniqueId())
        .location(locationConverter.apply(hardwareModel.getLocationModel()))
        .mbRam(hardwareModel.hardwareOffer().getMbOfRam())
        .gbDisk(hardwareModel.hardwareOffer().getDiskSpace())
        .cores(hardwareModel.hardwareOffer().getNumberOfCores()).build();

  }
}
