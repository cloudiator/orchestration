/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestQueue.class);
  private final BlockingQueue<NodeRequest> pendingRequests;

  public NodeRequestQueue() {
    pendingRequests = new LinkedBlockingQueue<>();
  }

  public void addRequest(NodeRequest nodeRequest) {
    LOGGER.debug(String
        .format("New node request %s added to queue. Pending requests currently %s.", nodeRequest,
            pendingRequests.size()));
    this.pendingRequests.add(nodeRequest);
  }

  NodeRequest takeRequest() throws InterruptedException {
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

    NodeRequestMessage getNodeRequestMessage() {
      return nodeRequestMessage;
    }

    String getId() {
      return id;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add(id, "id")
          .add("nodeRequestMessage", nodeRequestMessage).toString();
    }
  }
}
