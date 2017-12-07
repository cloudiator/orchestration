package org.cloudiator.iaas.node;

import com.google.inject.Singleton;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cloudiator.messages.Node.NodeRequestMessage;

@Singleton
public class NodeRequestQueue {

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
  }

  private final BlockingQueue<NodeRequest> pendingRequests;

  public NodeRequestQueue() {
    pendingRequests = new LinkedBlockingQueue<>();
  }

  public void addRequest(NodeRequest nodeRequest) {

    this.pendingRequests.add(nodeRequest);
  }

  public NodeRequest takeRequest() throws InterruptedException {
    return this.pendingRequests.take();
  }
}
