package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class VirtualMachineMessageToVirtualMachine implements TwoWayConverter<IaasEntities.VirtualMachine, VirtualMachine> {

  private LoginCredentialMessageToLoginCredential loginCredentialMessageToLoginCredential = new LoginCredentialMessageToLoginCredential();
  private ImageMessageToImageConverter imageMessageToImageConverter = new ImageMessageToImageConverter();


  @Override
  public IaasEntities.VirtualMachine applyBack(VirtualMachine virtualMachine) {

    /*
    return IaasEntities.VirtualMachine.newBuilder()
        .setId(virtualMachine.id())
        .setImage(virtualMachine.image())
        .setHardware(virtualMachine.hardware())
        .setLocation(virtualMachine.location())
        .setLoginCredential(loginCredentialMessageToLoginCredential.applyBack(virtualMachine))
        .build();

        */

    return null;
  }

  @Override
  public VirtualMachine apply(IaasEntities.VirtualMachine virtualMachine) {

    /*
    return VirtualMachineBuilder.newBuilder()
        .loginCredential(loginCredentialMessageToLoginCredential.apply(virtualMachine.getLoginCredential()))
        .id(virtualMachine.getId())
        .image(imageMessageToImageConverter.apply(virtualMachine.getImage()))
        .hardware(virtualMachine.getHardware())
        .location(virtualMachine.getLocation())
        .build();
        */
    return null;

  }
}
