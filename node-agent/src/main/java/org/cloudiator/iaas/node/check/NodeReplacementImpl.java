/*
 * Copyright (c) 2014-2020 University of Ulm
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

package org.cloudiator.iaas.node.check;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import java.util.concurrent.ExecutionException;
import org.cloudiator.iaas.node.NodeDeletionStrategy;
import org.cloudiator.iaas.node.NodeSchedulingException;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeReplacementImpl implements NodeReplacement {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeReplacementImpl.class);
  private final NodeService nodeService;
  private final NodeDeletionStrategy nodeDeletionStrategy;

  @Inject
  public NodeReplacementImpl(NodeService nodeService,
      NodeDeletionStrategy nodeDeletionStrategy) {
    this.nodeService = nodeService;
    this.nodeDeletionStrategy = nodeDeletionStrategy;
  }

  @Override
  public ForNodeReplacement forNode(Node node) {
    return new ForNodeReplacementImpl(node, nodeService, nodeDeletionStrategy);
  }

  public static class ForNodeReplacementImpl implements ForNodeReplacement {

    private final Node toBeReplaced;
    private final NodeService nodeService;
    private final NodeDeletionStrategy nodeDeletionStrategy;
    private static final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

    public ForNodeReplacementImpl(Node toBeReplaced,
        NodeService nodeService, NodeDeletionStrategy nodeDeletionStrategy) {
      this.toBeReplaced = toBeReplaced;
      this.nodeService = nodeService;
      this.nodeDeletionStrategy = nodeDeletionStrategy;
    }

    private boolean deleteOriginalNode() {

      try {
        nodeDeletionStrategy.deleteNode(toBeReplaced);
      } catch (Exception e) {
        LOGGER.warn("Deletion of replaced node " + toBeReplaced
            + " failed. This may lead to an orphaned resource");
        return false;
      }

      return true;

    }

    private Node allocateReplacement() throws NodeSchedulingException {

      if (!toBeReplaced.nodeCandidate().isPresent()) {
        throw new IllegalStateException("Can not executed replacement, node candidate not known.");
      }

      NodeRequestMessage nodeRequestMessage = NodeRequestMessage.newBuilder()
          .setUserId(toBeReplaced.userId())
          .setGroupName(toBeReplaced.name()).setNodeCandidate(toBeReplaced.nodeCandidate().get())
          .build();

      SettableFutureResponseCallback<NodeRequestResponse, Node> nodeCallback = SettableFutureResponseCallback
          .create(
              nodeRequestResponse -> NODE_CONVERTER.applyBack(nodeRequestResponse.getNode()));

      nodeService.createNodeAsync(nodeRequestMessage, nodeCallback);

      try {
        return nodeCallback.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new NodeSchedulingException("Error while allocating replacement", e);
      }

    }

    @Override
    public Node execute() throws NodeSchedulingException {

      LOGGER.info(String.format("Allocating replacement node for %s.", toBeReplaced));
      final Node replaced = allocateReplacement();
      LOGGER.info(String.format("Node %s is now replaced with node %s.", toBeReplaced, replaced));
      final boolean b = deleteOriginalNode();
      if (b) {
        LOGGER.debug(String.format("Successfully deleted original node %s.", toBeReplaced));
      }

      return replaced;
    }
  }
}
