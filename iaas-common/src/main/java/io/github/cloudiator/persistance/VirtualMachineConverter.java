package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.messaging.HardwareMessageRepository;
import io.github.cloudiator.messaging.ImageMessageRepository;
import io.github.cloudiator.messaging.LocationMessageRepository;
import javax.annotation.Nullable;

class VirtualMachineConverter implements
    OneWayConverter<VirtualMachineModel, VirtualMachine> {

  private final HardwareMessageRepository hardwareMessageRepository;
  private final LocationMessageRepository locationMessageRepository;
  private final ImageMessageRepository imageMessageRepository;
  private final LoginCredentialConverter loginCredentialConverter = new LoginCredentialConverter();
  private final IpAddressConverter ipAddressConverter = new IpAddressConverter();

  @Inject VirtualMachineConverter(
      HardwareMessageRepository hardwareMessageRepository,
      LocationMessageRepository locationMessageRepository,
      ImageMessageRepository imageMessageRepository) {
    this.hardwareMessageRepository = hardwareMessageRepository;
    this.locationMessageRepository = locationMessageRepository;
    this.imageMessageRepository = imageMessageRepository;
  }

  private HardwareFlavor getHardwareFlavor(VirtualMachineModel virtualMachineModel) {
    if (virtualMachineModel.getHardwareId() == null) {
      return null;
    }
    return hardwareMessageRepository
        .getById(virtualMachineModel.getTenantModel().getUserId(),
            virtualMachineModel.getHardwareId());
  }

  private Image getImage(VirtualMachineModel virtualMachineModel) {
    if (virtualMachineModel.getImageId() == null) {
      return null;
    }
    return imageMessageRepository.getById(virtualMachineModel.getTenantModel().getUserId(),
        virtualMachineModel.getImageId());
  }

  private Location getLocation(VirtualMachineModel virtualMachineModel) {
    if (virtualMachineModel.getLocationId() == null) {
      return null;
    }
    return locationMessageRepository.getById(virtualMachineModel.getTenantModel().getUserId(),
        virtualMachineModel.getLocationId());
  }


  @Nullable
  @Override
  public VirtualMachine apply(@Nullable VirtualMachineModel virtualMachineModel) {
    if (virtualMachineModel == null) {
      return null;
    }

    final VirtualMachineBuilder virtualMachineBuilder = VirtualMachineBuilder.newBuilder()
        .loginCredential(loginCredentialConverter.apply(virtualMachineModel.getLoginCredential()))
        .hardware(getHardwareFlavor(virtualMachineModel))
        .image(getImage(virtualMachineModel))
        .location(getLocation(virtualMachineModel))
        .id(virtualMachineModel.getCloudUniqueId()).providerId(virtualMachineModel.getProviderId())
        .name(virtualMachineModel.getName());

    virtualMachineModel.ipAddresses().forEach(
        ipAddressModel -> virtualMachineBuilder
            .addIpAddress(ipAddressConverter.apply(ipAddressModel)));

    return virtualMachineBuilder.build();
  }
}
