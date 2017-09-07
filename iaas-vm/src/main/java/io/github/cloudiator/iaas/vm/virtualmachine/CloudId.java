package io.github.cloudiator.iaas.vm.virtualmachine;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByCloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import java.util.function.Function;

public class CloudId implements Function<VirtualMachineTemplate, String> {

  public static CloudId instance() {
    return new CloudId();
  }

  @Override
  public String apply(VirtualMachineTemplate virtualMachineTemplate) {
    IdScopedByCloud image = IdScopedByClouds.from(virtualMachineTemplate.imageId());
    IdScopedByCloud location = IdScopedByClouds.from(virtualMachineTemplate.locationId());
    IdScopedByCloud hardware = IdScopedByClouds.from(virtualMachineTemplate.hardwareFlavorId());

    if (image.cloudId().equals(location.cloudId()) && location.cloudId()
        .equals(hardware.cloudId())) {
      return image.cloudId();
    }
    throw new IllegalStateException(String
        .format("CloudIds are not equal. Image: %s, Location %s, Hardware %s.", image.cloudId(),
            location.cloudId(), hardware.cloudId()));

  }
}
