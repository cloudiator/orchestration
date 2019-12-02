/*
 * Copyright (c) 2014-2019 University of Ulm
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

package org.cloudiator.iaas.node.messaging;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeState;
import org.cloudiator.iaas.node.NodeStateMachine;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messages.Node.NodeRequestResponse;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteNodeWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DeleteNodeWorker.class);

  private final DeleteNodeRequest deleteNodeRequest;

  private final MessageInterface messageInterface;
  private final NodeStateMachine nodeStateMachine;

  @Inject
  public DeleteNodeWorker(@Assisted DeleteNodeRequest deleteNodeRequest,
      MessageInterface messageInterface,
      NodeStateMachine nodeStateMachine) {
    this.deleteNodeRequest = deleteNodeRequest;
    this.messageInterface = messageInterface;
    this.nodeStateMachine = nodeStateMachine;
  }

  @Override
  public void run() {

    final Node node = deleteNodeRequest.getNode();
    final String requestId = deleteNodeRequest.getId();

    try {

      LOGGER.debug(
          String.format(
              "%s is deleting node %s.",
              this, node));

      final Node deleted = nodeStateMachine
          .apply(node, NodeState.DELETED, null);

      LOGGER.debug(
          String.format("%s is replying success as it deleted node %s.", this, deleted));

      messageInterface.reply(requestId, NodeDeleteResponseMessage.newBuilder().build());
    } catch (Exception e) {
      LOGGER.error(String
          .format("Unexpected error %s occurred while deleting node %s.", e.getMessage(),
              node), e);
      messageInterface.reply(NodeRequestResponse.class, deleteNodeRequest.getId(),
          Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
    }

  }

  public static class DeleteNodeRequest {

    private final String requestId;
    private final Node node;

    private DeleteNodeRequest(String id,
        Node node) {
      this.requestId = id;
      this.node = node;
    }

    public static DeleteNodeRequest of(String requestId, Node node) {
      return new DeleteNodeRequest(requestId, node);
    }

    public String getId() {
      return requestId;
    }

    public Node getNode() {
      return node;
    }
  }
}
