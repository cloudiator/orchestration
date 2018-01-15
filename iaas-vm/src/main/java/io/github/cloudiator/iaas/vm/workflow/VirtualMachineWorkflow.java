package io.github.cloudiator.iaas.vm.workflow;

import de.uniulm.omi.cloudiator.sword.service.ComputeService;

/**
 * Created by daniel on 30.06.17.
 */
public class VirtualMachineWorkflow extends AbstractWorkflow {

  private final ComputeService computeService;

  public VirtualMachineWorkflow(
      ComputeService computeService) {
    this.computeService = computeService;
  }

  @Override
  protected void configure() {
    builder().addActivity(new AssignSecurityGroups())
        .addActivity(new StartVirtualMachineActivity(computeService))
        .addActivity(new AssignPublicIp(computeService)).build();
  }
}
