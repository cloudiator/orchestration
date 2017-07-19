package io.github.cloudiator.workflow;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;

/**
 * Created by daniel on 07.02.17.
 */
public class AssignPublicIp implements Activity {

  private final ComputeService computeService;
  private final static String NO_PUBLIC_IP = "Virtual machine %s has no public ip and public ip service is not supported for compute service %s.";

  public AssignPublicIp(ComputeService computeService) {
    checkNotNull(computeService, "computeService is null");
    this.computeService = computeService;
  }

  @Override
  public Exchange execute(Exchange input) {
    final VirtualMachine virtualMachine = input.getData(VirtualMachine.class).orElseThrow(
        () -> new IllegalStateException("Expected a virtual machine to be provided."));

    if (!virtualMachine.publicAddresses().isEmpty()) {
      return input;
    }

    checkState(computeService.publicIpExtension().isPresent(),
        String.format(NO_PUBLIC_IP, virtualMachine, computeService));
    return Exchange.of(VirtualMachineBuilder.of(virtualMachine).addPublicIpAddress(
        computeService.publicIpExtension().get().addPublicIp(virtualMachine.id())));
  }
}
