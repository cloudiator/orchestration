package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavorBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.HardwareFlavor.Builder;

/**
 * Created by daniel on 09.06.17.
 */
public class HardwareMessageToHardwareConverter implements
    TwoWayConverter<IaasEntities.HardwareFlavor, HardwareFlavor> {

  public static final HardwareMessageToHardwareConverter INSTANCE = new HardwareMessageToHardwareConverter();

  private static final LocationMessageToLocationConverter LOCATION_CONVERTER = LocationMessageToLocationConverter.INSTANCE;

  private HardwareMessageToHardwareConverter() {}

  @Override
  public IaasEntities.HardwareFlavor applyBack(HardwareFlavor hardwareFlavor) {
    Builder builder = IaasEntities.HardwareFlavor.newBuilder()
        .setCores(hardwareFlavor.numberOfCores())
        .setId(hardwareFlavor.id()).setProviderId(hardwareFlavor.providerId())
        .setRam(hardwareFlavor.mbRam()).setName(hardwareFlavor.name());
    if (hardwareFlavor.gbDisk().isPresent()) {
      builder.setDisk(hardwareFlavor.gbDisk().get());
    }
    if (hardwareFlavor.location().isPresent()) {
      builder.setLocation(LOCATION_CONVERTER.applyBack(hardwareFlavor.location().get()));
    }
    return builder.build();
  }

  @Override
  public HardwareFlavor apply(IaasEntities.HardwareFlavor hardwareFlavor) {

    final HardwareFlavorBuilder hardwareFlavorBuilder = HardwareFlavorBuilder.newBuilder()
        .cores(hardwareFlavor.getCores())
        .id(hardwareFlavor.getId()).name(hardwareFlavor.getName())
        .providerId(hardwareFlavor.getProviderId()).mbRam(hardwareFlavor.getRam());

    if (hardwareFlavor.hasLocation()) {
      hardwareFlavorBuilder.location(LOCATION_CONVERTER.apply(hardwareFlavor.getLocation()));
    }

    if (hardwareFlavor.getDisk() != 0) {
      hardwareFlavorBuilder.gbDisk(hardwareFlavor.getDisk());
    }

    return hardwareFlavorBuilder.build();
  }
}
