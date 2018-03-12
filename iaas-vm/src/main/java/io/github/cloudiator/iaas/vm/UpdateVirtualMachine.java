package io.github.cloudiator.iaas.vm;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import io.github.cloudiator.messaging.HardwareMessageRepository;
import io.github.cloudiator.messaging.ImageMessageRepository;
import io.github.cloudiator.messaging.LocationMessageRepository;

public class UpdateVirtualMachine {

  private final HardwareMessageRepository hardwareMessageRepository;
  private final LocationMessageRepository locationMessageRepository;
  private final ImageMessageRepository imageMessageRepository;

  @Inject
  public UpdateVirtualMachine(
      HardwareMessageRepository hardwareMessageRepository,
      LocationMessageRepository locationMessageRepository,
      ImageMessageRepository imageMessageRepository) {
    this.hardwareMessageRepository = hardwareMessageRepository;
    this.locationMessageRepository = locationMessageRepository;
    this.imageMessageRepository = imageMessageRepository;
  }

  public VirtualMachine update(String userId, VirtualMachineTemplate virtualMachineTemplate,
      VirtualMachine virtualMachine) {

    String hardwareId = virtualMachineTemplate.hardwareFlavorId();
    if (virtualMachine.hardware().isPresent()) {
      hardwareId = virtualMachine.hardware().get().id();
    }

    String imageId = virtualMachineTemplate.imageId();
    if (virtualMachine.image().isPresent()) {
      imageId = virtualMachine.image().get().id();
    }

    String locationId = virtualMachineTemplate.locationId();
    if (virtualMachine.location().isPresent()) {
      locationId = virtualMachine.location().get().id();
    }

    final HardwareFlavor hardware = hardwareMessageRepository.getById(userId, hardwareId);
    final Image image = imageMessageRepository.getById(userId, imageId);
    final Location location = locationMessageRepository.getById(userId, locationId);

    return VirtualMachineBuilder.of(virtualMachine).hardware(hardware).image(image)
        .location(location).build();
  }

}
