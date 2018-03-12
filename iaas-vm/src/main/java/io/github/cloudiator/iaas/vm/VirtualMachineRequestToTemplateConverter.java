package io.github.cloudiator.iaas.vm;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplateBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;

public class VirtualMachineRequestToTemplateConverter implements
    OneWayConverter<VirtualMachineRequest, VirtualMachineTemplate> {

  @Override
  public VirtualMachineTemplate apply(VirtualMachineRequest virtualMachineRequest) {

    if (virtualMachineRequest == null) {
      return null;
    }

    final VirtualMachineTemplateBuilder templateBuilder = VirtualMachineTemplateBuilder.newBuilder()
        .hardwareFlavor(virtualMachineRequest.getHardware())
        .image(virtualMachineRequest.getImage()).location(virtualMachineRequest.getLocation())
        .name(virtualMachineRequest.getName());
    return templateBuilder.build();
  }
}
