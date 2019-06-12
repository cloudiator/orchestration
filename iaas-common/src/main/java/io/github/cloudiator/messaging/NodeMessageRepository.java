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

package io.github.cloudiator.messaging;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Node.NodeDeleteMessage;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messages.Node.NodeQueryMessage;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.NodeService;

public class NodeMessageRepository implements MessageRepository<Node> {

  private final NodeService nodeService;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public NodeMessageRepository(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Nullable
  @Override
  public Node getById(String userId, String id) {

    final NodeQueryMessage nodeQueryMessage = NodeQueryMessage.newBuilder().setUserId(userId)
        .setNodeId(id)
        .build();

    try {
      return nodeService.queryNodes(nodeQueryMessage).getNodesList().stream().map(
          NODE_MESSAGE_CONVERTER::applyBack).collect(StreamUtil.getOnly()).orElse(null);
    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve nodes.", e);
    }
  }

  @Override
  public List<Node> getAll(String userId) {

    final NodeQueryMessage nodeQueryMessage = NodeQueryMessage.newBuilder().setUserId(userId)
        .build();

    try {
      return nodeService.queryNodes(nodeQueryMessage).getNodesList().stream()
          .map(NODE_MESSAGE_CONVERTER::applyBack).collect(Collectors
              .toList());
    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve nodes.", e);
    }
  }

  public Future<?> delete(String userId, String nodeId) {

    checkArgument(!Strings.isNullOrEmpty(userId), "userId is null or empty");
    checkArgument(!Strings.isNullOrEmpty(nodeId), "nodeId is null or empty");

    checkState(getById(userId, nodeId) != null,
        String.format("Node with id %s does not exist for user %s.", nodeId, userId));

    SettableFutureResponseCallback<NodeDeleteResponseMessage, NodeDeleteResponseMessage> futureResponseCallback = SettableFutureResponseCallback
        .create();

    nodeService
        .deleteNodeAsync(NodeDeleteMessage.newBuilder().setNodeId(nodeId).setUserId(userId).build(),
            futureResponseCallback);

    return futureResponseCallback;

  }
}
