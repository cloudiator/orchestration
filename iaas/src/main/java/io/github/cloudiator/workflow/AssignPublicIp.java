package io.github.cloudiator.workflow;

import de.uniulm.omi.cloudiator.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.api.service.ComputeService;
import de.uniulm.omi.cloudiator.domain.VirtualMachineBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by daniel on 07.02.17.
 */
public class AssignPublicIp implements Activity {

    private final ComputeService computeService;

    public AssignPublicIp(ComputeService computeService) {
        checkNotNull(computeService, "computeService is null");
        this.computeService = computeService;
    }

    @Override public Exchange execute(Exchange input) {
        final VirtualMachine virtualMachine = input.getData(VirtualMachine.class).orElseThrow(
            () -> new IllegalStateException("Expected a virtual machine to be provided."));

        if (!virtualMachine.publicAddresses().isEmpty()) {
            return input;
        }

        checkState(computeService.publicIpExtension().isPresent());
        return Exchange.of(VirtualMachineBuilder.of(virtualMachine).addPublicIpAddress(
            computeService.publicIpExtension().get().addPublicIp(virtualMachine.id())));
    }
}
