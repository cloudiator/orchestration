package io.github.cloudiator.workflow;

import de.uniulm.omi.cloudiator.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.api.service.ComputeService;

import static com.google.common.base.Preconditions.checkNotNull;

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
