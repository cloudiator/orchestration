package org.cloudiator.iaas.node;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.iaas.common.domain.Node;
import io.github.cloudiator.iaas.common.messaging.converters.NodeToNodeMessageConverter;
import javax.inject.Inject;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;

public class NodePublisher {

  private final MessageInterface messageInterface;
  private final VirtualMachineToNode virtualMachineToNode;
  private final NodeToNodeMessageConverter nodeConverter = new NodeToNodeMessageConverter();

  @Inject
  public NodePublisher(MessageInterface messageInterface,
      VirtualMachineToNode virtualMachineToNode) {
    this.messageInterface = messageInterface;
    this.virtualMachineToNode = virtualMachineToNode;
  }


  public void publish(
      VirtualMachine virtualMachine, String userId) throws ResponseException {

    final Node node = virtualMachineToNode
        .apply(virtualMachine);

    messageInterface.publish(NodeEvent.newBuilder().setNode(nodeConverter.apply(node)).build());
  }
}
