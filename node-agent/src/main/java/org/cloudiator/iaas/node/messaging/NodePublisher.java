package org.cloudiator.iaas.node.messaging;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import javax.inject.Inject;
import org.cloudiator.iaas.node.VirtualMachineToNode;
import org.cloudiator.messages.NodeEntities.NodeEvent;
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
