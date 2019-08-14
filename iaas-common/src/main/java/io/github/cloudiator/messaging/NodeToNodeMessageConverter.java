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

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import java.util.stream.Collectors;
import org.cloudiator.messages.NodeEntities;

public class NodeToNodeMessageConverter implements TwoWayConverter<Node, NodeEntities.Node> {

  public static final NodeToNodeMessageConverter INSTANCE = new NodeToNodeMessageConverter();

  private static final IpAddressMessageToIpAddress IP_ADDRESS_CONVERTER = new IpAddressMessageToIpAddress();
  private static final NodeTypeToNodeTypeMessage NODE_TYPE_CONVERTER = new NodeTypeToNodeTypeMessage();

  private static final NodePropertiesMessageToNodePropertiesConverter NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();
  private static final LoginCredentialMessageToLoginCredentialConverter LOGIN_CREDENTIAL_CONVERTER = LoginCredentialMessageToLoginCredentialConverter.INSTANCE;

  public static final NodeStateConverter NODE_STATE_CONVERTER = new NodeStateConverter();

  private NodeToNodeMessageConverter() {
  }

  @Override
  public Node applyBack(NodeEntities.Node node) {

    final NodeBuilder nodeBuilder = NodeBuilder.newBuilder()
        .id(node.getId())
        .name(node.getName())
        .nodeProperties(NODE_PROPERTIES_CONVERTER.apply(node.getNodeProperties()))
        .nodeType(NODE_TYPE_CONVERTER.applyBack(node.getNodeType())).ipAddresses(
            node.getIpAddressesList().stream().map(IP_ADDRESS_CONVERTER)
                .collect(Collectors.toSet()))
        .state(NODE_STATE_CONVERTER.applyBack(node.getState()));

    if (node.hasLoginCredential()) {
      nodeBuilder.loginCredential(LOGIN_CREDENTIAL_CONVERTER.apply(node.getLoginCredential()));
    }

    if (!Strings.isNullOrEmpty(node.getReason())) {
      nodeBuilder.reason(node.getReason());
    }

    if (!Strings.isNullOrEmpty(node.getDiagnostic())) {
      nodeBuilder.diagnostic(node.getDiagnostic());
    }

    if (!Strings.isNullOrEmpty(node.getNodeCandidate())) {
      nodeBuilder.nodeCandidate(node.getNodeCandidate());
    }

    if (!Strings.isNullOrEmpty(node.getUserId())) {
      nodeBuilder.userId(node.getUserId());
    }

    if (!Strings.isNullOrEmpty(node.getOriginId())) {
      nodeBuilder.originId(node.getOriginId());
    }

    return nodeBuilder.build();
  }

  @Override
  public NodeEntities.Node apply(Node node) {

    final NodeEntities.Node.Builder builder = NodeEntities.Node.newBuilder().setId(node.id())
        .setName(node.name())
        .addAllIpAddresses(
            node.ipAddresses().stream().map(IP_ADDRESS_CONVERTER::applyBack)
                .collect(Collectors.toList()))
        .setNodeProperties(NODE_PROPERTIES_CONVERTER.applyBack(node.nodeProperties()))
        .setNodeType(NODE_TYPE_CONVERTER.apply(node.type()))
        .setState(NODE_STATE_CONVERTER.apply(node.state()))
        .setUserId(node.userId());

    if (node.diagnostic().isPresent()) {
      builder.setDiagnostic(node.diagnostic().get());
    }

    if (node.reason().isPresent()) {
      builder.setReason(node.reason().get());
    }

    if (node.nodeCandidate().isPresent()) {
      builder.setNodeCandidate(node.nodeCandidate().get());
    }

    if (node.loginCredential().isPresent()) {
      builder
          .setLoginCredential(LOGIN_CREDENTIAL_CONVERTER.applyBack(node.loginCredential().get()));
    }

    if (node.originId().isPresent()) {
      builder.setOriginId(node.originId().get());
    }

    return builder.build();
  }
}
