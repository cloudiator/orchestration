package org.cloudiator.iaas.node;

import de.uniulm.omi.cloudiator.domain.LoginNameSupplier.UnknownLoginNameException;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeType;
import java.util.function.Function;

public class VirtualMachineToNode implements Function<VirtualMachine, Node> {

  public static final VirtualMachineToNode INSTANCE = new VirtualMachineToNode();

  private VirtualMachineToNode() {
  }

  @Override
  public Node apply(VirtualMachine virtualMachine) {

    NodeProperties nodeProperties = NodePropertiesBuilder
        .of(virtualMachine.hardware().get(), virtualMachine.image().get(),
            virtualMachine.location().get())
        .build();

    LoginCredential loginCredential = null;

    if (virtualMachine.loginCredential().isPresent()) {
      loginCredential = virtualMachine.loginCredential().get();
      if (!loginCredential.username().isPresent()) {
        if (virtualMachine.image().isPresent()) {
          final OperatingSystem operatingSystem = virtualMachine.image().get().operatingSystem();
          try {
            final String loginName = operatingSystem.operatingSystemFamily().loginName();
            loginCredential = LoginCredentialBuilder.of(loginCredential).username(loginName)
                .build();
          } catch (UnknownLoginNameException ignored) {
            //left empty
          }
        }
      }
    }

    return NodeBuilder.newBuilder().nodeType(NodeType.VM).ipAddresses(virtualMachine.ipAddresses())
        .loginCredential(loginCredential)
        .nodeProperties(nodeProperties).id(virtualMachine.id()).name(virtualMachine.name()).build();
  }
}
