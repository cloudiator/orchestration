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

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.domain.NodeGroups;
import java.util.LinkedList;
import java.util.List;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.NodeGroup.Builder;

public class NodeGroupMessageToNodeGroup implements
    TwoWayConverter<NodeEntities.NodeGroup, NodeGroup> {

  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;

  @Override
  public NodeEntities.NodeGroup applyBack(NodeGroup nodeGroup) {

    final Builder builder = NodeEntities.NodeGroup.newBuilder();
    for (final Node node : nodeGroup.getNodes()) {
      builder.addNodes(NODE_MESSAGE_CONVERTER.apply(node));
    }
    builder.setId(nodeGroup.id());

    return builder.build();
  }

  @Override
  public NodeGroup apply(NodeEntities.NodeGroup nodeGroup) {

    List<Node> nodes = new LinkedList<>();
    for (NodeEntities.Node node : nodeGroup.getNodesList()) {
      nodes.add(NODE_MESSAGE_CONVERTER.applyBack(node));
    }

    return NodeGroups.of(nodeGroup.getId(), nodes);
  }
}
