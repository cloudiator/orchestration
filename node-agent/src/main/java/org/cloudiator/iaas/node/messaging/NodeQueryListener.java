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

package org.cloudiator.iaas.node.messaging;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.NodeDomainRepository;
import java.util.List;
import javax.annotation.Nullable;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeQueryMessage;
import org.cloudiator.messages.Node.NodeQueryResponse;
import org.cloudiator.messages.Node.NodeQueryResponse.Builder;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeQueryListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeQueryListener.class);
  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private static final NodeToNodeMessageConverter NODE_TO_NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public NodeQueryListener(MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository) {
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeQueryMessage.class, NodeQueryMessage.parser(),
        new MessageCallback<NodeQueryMessage>() {
          @Override
          public void accept(String id, NodeQueryMessage content) {
            try {

              if (content.getUserId() == null || content.getUserId().isEmpty()) {
                messageInterface.reply(id,
                    Error.newBuilder().setCode(500).setMessage("No userId was provided").build());
              }

              final NodeQueryResponse nodeQueryResponse = handleResponse(content.getNodeId(),
                  content.getUserId());
              messageInterface.reply(id, nodeQueryResponse);
            } catch (Exception e) {
              LOGGER.error("Unexpected error while querying nodes", e);
              messageInterface.reply(id, Error.newBuilder().setCode(500).setMessage(
                  "Unexpected error while querying nodes. Message was " + e.getMessage()).build());
            }

          }
        });
  }

  @Transactional
  private NodeQueryResponse handleResponse(@Nullable String id, String userId) {
    checkNotNull(userId, "userId is null");
    if (id == null || id.isEmpty()) {
      return handleMultiple(userId);
    }
    return handleSingle(id, userId);
  }

  private NodeQueryResponse handleSingle(String id, String userId) {
    checkNotNull(id, "id is null");

    final Node node = nodeDomainRepository.findByTenantAndId(userId, id);
    if (node == null) {
      return NodeQueryResponse.newBuilder().build();
    }
    return NodeQueryResponse.newBuilder().addNodes(NODE_TO_NODE_MESSAGE_CONVERTER.apply(node)).build();
  }

  private NodeQueryResponse handleMultiple(String userId) {

    final List<Node> nodes = nodeDomainRepository.findByTenant(userId);

    final Builder builder = NodeQueryResponse.newBuilder();

    nodes.stream().map(NODE_TO_NODE_MESSAGE_CONVERTER).forEach(
        builder::addNodes);

    return builder.build();
  }


}
