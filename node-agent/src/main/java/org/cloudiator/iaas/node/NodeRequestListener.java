package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import io.github.cloudiator.iaas.common.messaging.NodeRequestConverter;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messaging.MessageInterface;

public class NodeRequestListener implements Runnable {

  private final MessageInterface messageInterface;
  private final NodeRequestQueue nodeRequestQueue;
  private final NodeRequestConverter nodeRequestConverter = new NodeRequestConverter();

  @Inject
  public NodeRequestListener(MessageInterface messageInterface,
      NodeRequestQueue nodeRequestQueue) {
    this.messageInterface = messageInterface;
    this.nodeRequestQueue = nodeRequestQueue;
  }


  @Override
  public void run() {
    messageInterface.subscribe(NodeRequestMessage.class, NodeRequestMessage.parser(),
        (id, content) -> nodeRequestQueue
            .addRequest(content.getUserId(), id,
                nodeRequestConverter.apply(content.getNodeRequest())));
  }
}
