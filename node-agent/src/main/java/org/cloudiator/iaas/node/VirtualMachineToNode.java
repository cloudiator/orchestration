package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.iaas.common.domain.Node;
import io.github.cloudiator.iaas.common.domain.NodeBuilder;
import io.github.cloudiator.iaas.common.domain.NodeProperties;
import io.github.cloudiator.iaas.common.domain.NodePropertiesBuilder;
import io.github.cloudiator.iaas.common.domain.NodeType;
import java.util.function.Function;

public class VirtualMachineToNode implements Function<VirtualMachine, Node> {

  @Inject
  public VirtualMachineToNode() {
  }

  @Override
  public Node apply(VirtualMachine virtualMachine) {

    NodeProperties nodeProperties = NodePropertiesBuilder
        .of(virtualMachine.hardware().get(), virtualMachine.image().get(),
            virtualMachine.location().get())
        .build();
    return NodeBuilder.newBuilder().nodeType(NodeType.VM).ipAddresses(virtualMachine.ipAddresses())
        .loginCredential(virtualMachine.loginCredential().orElse(null))
        .nodeProperties(nodeProperties).id(virtualMachine.id()).build();
  }
}