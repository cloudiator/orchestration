package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine.Builder;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class VirtualMachineMessageToVirtualMachine implements
    TwoWayConverter<IaasEntities.VirtualMachine, VirtualMachine> {

  private LoginCredentialMessageToLoginCredentialConverter loginCredentialConverter = new LoginCredentialMessageToLoginCredentialConverter();
  private ImageMessageToImageConverter imageConverter = new ImageMessageToImageConverter();
  private HardwareMessageToHardwareConverter hardwareConverter = new HardwareMessageToHardwareConverter();
  private LocationMessageToLocationConverter locationConverter = new LocationMessageToLocationConverter();
  private IpAddressMessageToIpAddress ipConverter = new IpAddressMessageToIpAddress();


  @Override
  public IaasEntities.VirtualMachine applyBack(VirtualMachine virtualMachine) {

    final Builder builder = IaasEntities.VirtualMachine.newBuilder()
        .setId(virtualMachine.id())
        .setProviderId(virtualMachine.providerId())
        .setName(virtualMachine.name());

    if (virtualMachine.location().isPresent()) {
      builder.setLocation(locationConverter.applyBack(virtualMachine.location().get()));
    }

    if (virtualMachine.image().isPresent()) {
      builder.setImage(imageConverter.applyBack(virtualMachine.image().get()));
    }

    if (virtualMachine.hardware().isPresent()) {
      builder.setHardware(hardwareConverter.applyBack(virtualMachine.hardware().get()));
    }

    if (virtualMachine.loginCredential().isPresent()) {
      builder.setLoginCredential(
          loginCredentialConverter.applyBack(virtualMachine.loginCredential().get()));
    }

    virtualMachine.ipAddresses().forEach(
        ipAddress -> builder.addIpAddresses(ipConverter.applyBack(ipAddress)));

    return builder.build();
  }

  @Override
  public VirtualMachine apply(IaasEntities.VirtualMachine virtualMachine) {

    VirtualMachineBuilder builder = VirtualMachineBuilder.newBuilder().id(virtualMachine.getId())
        .providerId(virtualMachine.getProviderId()).name(virtualMachine.getName());

    if (virtualMachine.hasImage()) {
      builder.image(imageConverter.apply(virtualMachine.getImage()));
    }

    if (virtualMachine.hasLocation()) {
      builder.location(locationConverter.apply(virtualMachine.getLocation()));
    }

    if (virtualMachine.hasHardware()) {
      builder.hardware(hardwareConverter.apply(virtualMachine.getHardware()));
    }

    if (virtualMachine.hasLoginCredential()) {
      builder.loginCredential(loginCredentialConverter.apply(virtualMachine.getLoginCredential()));
    }

    virtualMachine.getIpAddressesList().forEach(
        ipAddress -> builder.addIpAddress(ipConverter.apply(ipAddress)));

    return builder.build();

  }
}
