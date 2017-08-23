package io.github.cloudiator.virtualmachine;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplateBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;

public class VirtualMachineRequestToTemplateConverter implements
    OneWayConverter<VirtualMachineRequest, VirtualMachineTemplate> {

  @Nullable
  @Override
  public VirtualMachineTemplate apply(@Nullable VirtualMachineRequest virtualMachineRequest) {

    if (virtualMachineRequest == null) {
      return null;
    }

    return VirtualMachineTemplateBuilder.newBuilder()
        .hardwareFlavor(virtualMachineRequest.getHardware())
        .image(virtualMachineRequest.getImage()).location(virtualMachineRequest.getLocation())
        .name("test").build();
  }
}
