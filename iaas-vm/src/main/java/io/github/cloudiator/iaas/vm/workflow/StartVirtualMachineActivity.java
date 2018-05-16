package io.github.cloudiator.iaas.vm.workflow;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 07.02.17.
 */
public class StartVirtualMachineActivity implements Activity {

  private final ComputeService computeService;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(StartVirtualMachineActivity.class);

  public StartVirtualMachineActivity(ComputeService computeService) {
    checkNotNull(computeService, "computeService is null");
    this.computeService = computeService;
  }

  @Override
  public Exchange execute(Exchange input) {
    VirtualMachineTemplate virtualMachineTemplate = input.getData(VirtualMachineTemplate.class)
        .orElseThrow(() -> new IllegalStateException(
            "Expected a VirtualMachineTemplate to be provided"));

    LOGGER.debug(String
        .format("Starting execution of StartVirtualMachineActivity %s using template %s.",
            this, virtualMachineTemplate));

    final VirtualMachine virtualMachine = computeService
        .createVirtualMachine(virtualMachineTemplate);

    LOGGER.debug(String
        .format("StartVirtualMachineActivity %s create virtual machine %s.",
            this, virtualMachine));

    return Exchange.of(virtualMachine);
  }
}
