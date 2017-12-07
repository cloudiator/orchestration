package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import org.cloudiator.iaas.node.NodeRequestQueue.NodeRequest;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messaging.MessageInterface;

public class NodeRequestListener implements Runnable {

  private final MessageInterface messageInterface;
  private final NodeRequestQueue nodeRequestQueue;

  @Inject
  public NodeRequestListener(MessageInterface messageInterface,
      NodeRequestQueue nodeRequestQueue) {
    this.messageInterface = messageInterface;
    this.nodeRequestQueue = nodeRequestQueue;
  }


  @Override
  public void run() {
    messageInterface.subscribe(NodeRequestMessage.class, NodeRequestMessage.parser(),
        (id, content) -> nodeRequestQueue.addRequest(NodeRequest.of(content, id)));
  }
}
