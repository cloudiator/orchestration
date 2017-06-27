package io.github.cloudiator.iaas.common.persistance.messaging.converters;

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

  private final LocationMessageToLocationConverter locationConverter = new LocationMessageToLocationConverter();

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
      builder.setLocation(locationConverter.applyBack(hardwareFlavor.location().get()));
    }
    return builder.build();
  }

  @Override
  public HardwareFlavor apply(IaasEntities.HardwareFlavor hardwareFlavor) {

    return HardwareFlavorBuilder.newBuilder().cores(hardwareFlavor.getCores())
        .gbDisk(hardwareFlavor.getDisk()).id(hardwareFlavor.getId()).name(hardwareFlavor.getName())
        .providerId(hardwareFlavor.getProviderId()).mbRam(hardwareFlavor.getRam())
        .location(locationConverter.apply(hardwareFlavor.getLocation())).build();
  }
}
