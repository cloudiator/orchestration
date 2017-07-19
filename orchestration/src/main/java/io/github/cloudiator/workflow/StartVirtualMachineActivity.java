package io.github.cloudiator.workflow;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;

/**
 * Created by daniel on 07.02.17.
 */
public class CreateVirtualMachineActivity implements Activity {

    private final ComputeService computeService;

    public CreateVirtualMachineActivity(ComputeService computeService) {
        checkNotNull(computeService, "computeService is null");
        this.computeService = computeService;
    }

    @Override public Exchange execute(Exchange input) {
        VirtualMachineTemplate virtualMachineTemplate = input.getData(VirtualMachineTemplate.class)
            .orElseThrow(() -> new IllegalStateException(
                "Expected a VirtualMachineTemplate to be provided"));
        return Exchange.of(computeService.createVirtualMachine(virtualMachineTemplate));
    }
}
