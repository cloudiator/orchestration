package org.cloudiator.iaas.node;

import com.google.inject.Singleton;
import io.github.cloudiator.iaas.common.domain.NodeRequest;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class NodeRequestQueue {

  private final BlockingQueue<UserNodeRequest> pendingRequests;

  public static class UserNodeRequest {

    private final String userId;
    private final String messageId;
    private final NodeRequest nodeRequest;

    private static UserNodeRequest of(String userId, String messageId, NodeRequest nodeRequest) {
      return new UserNodeRequest(userId, messageId, nodeRequest);
    }

    private UserNodeRequest(String userId, String messageId, NodeRequest nodeRequest) {
      this.userId = userId;
      this.nodeRequest = nodeRequest;
      this.messageId = messageId;
    }

    public String getUserId() {
      return userId;
    }

    public NodeRequest getNodeRequest() {
      return nodeRequest;
    }

    public String getMessageId() {
      return messageId;
    }
  }

  public NodeRequestQueue() {
    pendingRequests = new LinkedBlockingQueue<>();
  }

  public void addRequest(String userId, String messageId, NodeRequest nodeRequest) {

    this.pendingRequests.add(UserNodeRequest.of(userId, messageId, nodeRequest));
  }

  public UserNodeRequest takeRequest() throws InterruptedException {
    return this.pendingRequests.take();
  }
}
