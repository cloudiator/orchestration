package io.github.cloudiator.iaas.vm.workflow;

import de.uniulm.omi.cloudiator.sword.domain.TemplateOptions;
import de.uniulm.omi.cloudiator.sword.domain.TemplateOptionsBuilder;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplateBuilder;
import io.github.cloudiator.iaas.common.util.SecurityGroupPorts;

public class AssignSecurityGroups implements Activity {

  @Override
  public Exchange execute(Exchange input) {
    VirtualMachineTemplate virtualMachineTemplate = input.getData(VirtualMachineTemplate.class)
        .orElseThrow(() -> new IllegalStateException("Expected virtual machine template."));

    TemplateOptionsBuilder builder;

    if (!virtualMachineTemplate.templateOptions().isPresent()) {
      builder = TemplateOptionsBuilder.newBuilder();
    } else {
      builder = TemplateOptionsBuilder.of(virtualMachineTemplate.templateOptions().get());
    }

    TemplateOptions templateOptions = builder
        .inboundPorts(SecurityGroupPorts.inBoundPorts())
        .build();

    VirtualMachineTemplate newTemplate = VirtualMachineTemplateBuilder.of(virtualMachineTemplate)
        .templateOptions(templateOptions).build();

    return Exchange.of(newTemplate);

  }
}
