package io.github.cloudiator.workflow;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;

/**
 * Created by daniel on 30.06.17.
 */
public class VirtualMachineWorkflow extends AbstractWorkflow {

  private final ComputeService computeService;

  @Inject
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
