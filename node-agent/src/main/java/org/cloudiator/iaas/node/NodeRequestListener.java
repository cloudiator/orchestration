package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import org.cloudiator.iaas.node.NodeRequestQueue.NodeRequest;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRequestListener implements Runnable {

  private final MessageInterface messageInterface;
  private final NodeRequestQueue nodeRequestQueue;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestListener.class);

  @Inject
  public NodeRequestListener(MessageInterface messageInterface,
      NodeRequestQueue nodeRequestQueue) {
    this.messageInterface = messageInterface;
    this.nodeRequestQueue = nodeRequestQueue;
  }


  @Override
  public void run() {
    messageInterface.subscribe(NodeRequestMessage.class, NodeRequestMessage.parser(),
        (id, content) -> {
          LOGGER.info(String.format("Receiving new node request %s. Adding to queue.", content));
          nodeRequestQueue.addRequest(NodeRequest.of(content, id));
        });
  }
}
