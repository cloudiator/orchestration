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

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.messaging.NodeGroupMessageToNodeGroup;
import io.github.cloudiator.persistance.NodeDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeGroupQueryMessage;
import org.cloudiator.messages.Node.NodeGroupQueryResponse;
import org.cloudiator.messages.Node.NodeGroupQueryResponse.Builder;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeGroupQueryListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeGroupQueryListener.class);
  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeGroupMessageToNodeGroup nodeGroupMessageToNodeGroup = new NodeGroupMessageToNodeGroup();

  @Inject
  public NodeGroupQueryListener(MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository) {
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeGroupQueryMessage.class, NodeGroupQueryMessage.parser(),
        new MessageCallback<NodeGroupQueryMessage>() {
          @Override
          public void accept(String id, NodeGroupQueryMessage content) {
            try {
              if (content.getUserId() == null || content.getUserId().isEmpty()) {
                messageInterface.reply(id,
                    Error.newBuilder().setCode(500).setMessage("No userId was provided.").build());
              }

              final NodeGroupQueryResponse nodeGroupQueryResponse = handleResponse(content);
              messageInterface.reply(id, nodeGroupQueryResponse);

            } catch (Exception e) {
              LOGGER.error("Unexpected exception while querying node groups.", e);
              messageInterface.reply(id, Error.newBuilder().setCode(500).setMessage(
                  "Unexpected exception while querying node groups. Error was " + e.getMessage())
                  .build());
            }
          }
        });
  }

  @Transactional
  private NodeGroupQueryResponse handleResponse(NodeGroupQueryMessage nodeGroupQueryMessage) {
    final String userId = nodeGroupQueryMessage.getUserId();
    final String id = nodeGroupQueryMessage.getNodeGroupId();

    if (id == null || id.isEmpty()) {
      return handleMultiple(userId);
    }
    return handleSingle(userId, id);


  }

  private NodeGroupQueryResponse handleSingle(String userId, String id) {

    final NodeGroup group = nodeDomainRepository.findGroupByTenantAndId(userId, id);
    if (group == null) {
      return NodeGroupQueryResponse.newBuilder().build();
    }

    return NodeGroupQueryResponse.newBuilder().addNodeGroups(nodeGroupMessageToNodeGroup
        .applyBack(group)).build();
  }

  private NodeGroupQueryResponse handleMultiple(String userId) {
    final Builder builder = NodeGroupQueryResponse.newBuilder();

    nodeDomainRepository.findGroupsByTenant(userId).stream()
        .map(nodeGroupMessageToNodeGroup::applyBack).forEach(builder::addNodeGroups);

    return builder.build();

  }

}
