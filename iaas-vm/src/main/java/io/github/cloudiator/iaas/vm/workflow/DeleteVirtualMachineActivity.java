package io.github.cloudiator.iaas.vm.workflow;

import static com.google.common.base.Preconditions.checkState;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;

public class DeleteVirtualMachineActivity implements Activity {

  private final ComputeService computeService;

  public DeleteVirtualMachineActivity(
      ComputeService computeService) {
    this.computeService = computeService;
  }

  @Override
  public Exchange execute(Exchange input) {
    String vmId = input.getData(String.class)
        .orElseThrow(() -> new IllegalStateException("Expected a virtual machine id."));

    VirtualMachine virtualMachine = computeService.discoveryService().getVirtualMachine(vmId);

    checkState(virtualMachine != null,
        String.format("Virtual machine with id %s does not exist.", vmId));

    computeService.deleteVirtualMachine(vmId);

    return Exchange.of(Void.class);

  }
}
