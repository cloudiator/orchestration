package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class VirtualMachineConverter implements
    OneWayConverter<VirtualMachineModel, VirtualMachine> {


  @Nullable
  @Override
  public VirtualMachine apply(@Nullable VirtualMachineModel virtualMachineModel) {
    return null;
  }
}
