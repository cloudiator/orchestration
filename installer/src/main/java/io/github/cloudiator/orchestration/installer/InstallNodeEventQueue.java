package io.github.cloudiator.orchestration.installer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messages.NodeEntities.Node;

/**
 * Created by Daniel Seybold on 20.07.2017.
 */
public class InstallNodeEventQueue {

  private final BlockingQueue<NodeEventItem> pendingRequests;

  public InstallNodeEventQueue() {
    this.pendingRequests = new LinkedBlockingQueue<>();
  }

  public NodeEventItem take() throws InterruptedException {
    return pendingRequests.take();
  }

  public void add(String requestId, NodeEvent request) {
    pendingRequests.add(new NodeEventItem(requestId, request.getNode()));
  }

  static class NodeEventItem {

    private final String requestId;
    private final Node node;

    private NodeEventItem(String requestId,
        Node node) {
      checkNotNull(requestId, "requestId is null");
      checkArgument(!requestId.isEmpty(), "requestId is empty");
      this.requestId = requestId;
      checkNotNull(node, "nodeEvent is null");
      this.node = node;

    }

    public String requestId() {
      return requestId;
    }

    public Node node() {
      return node;
    }


  }

}
