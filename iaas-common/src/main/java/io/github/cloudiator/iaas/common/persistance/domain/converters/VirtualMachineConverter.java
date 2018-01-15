package io.github.cloudiator.iaas.common.persistance.domain.converters;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.iaas.common.persistance.entities.VirtualMachineModel;
import javax.annotation.Nullable;

public class VirtualMachineConverter implements
    OneWayConverter<VirtualMachineModel, VirtualMachine> {


  @Nullable
  @Override
  public VirtualMachine apply(@Nullable VirtualMachineModel virtualMachineModel) {
    return null;
  }
}
