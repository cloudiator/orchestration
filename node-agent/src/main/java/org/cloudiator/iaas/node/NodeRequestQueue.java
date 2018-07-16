package org.cloudiator.iaas.node;

import com.google.common.base.MoreObjects;
import com.google.inject.Singleton;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeRequestQueue {

  private final BlockingQueue<NodeRequest> pendingRequests;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestQueue.class);

  public NodeRequestQueue() {
    pendingRequests = new LinkedBlockingQueue<>();
  }

  public void addRequest(NodeRequest nodeRequest) {
    LOGGER.debug(String
        .format("New node request %s added to queue. Pending requests currently %s.", nodeRequest,
            pendingRequests.size()));
    this.pendingRequests.add(nodeRequest);
  }

  public NodeRequest takeRequest() throws InterruptedException {
    return this.pendingRequests.take();
  }

  public static class NodeRequest {

    private final NodeRequestMessage nodeRequestMessage;
    private final String id;

    private NodeRequest(NodeRequestMessage nodeRequestMessage, String id) {
      this.nodeRequestMessage = nodeRequestMessage;
      this.id = id;
    }

    public static NodeRequest of(NodeRequestMessage nodeRequestMessage, String id) {
      return new NodeRequest(nodeRequestMessage, id);
    }

    public NodeRequestMessage getNodeRequestMessage() {
      return nodeRequestMessage;
    }

    public String getId() {
      return id;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add(id, "id")
          .add("nodeRequestMessage", nodeRequestMessage).toString();
    }
  }
}
