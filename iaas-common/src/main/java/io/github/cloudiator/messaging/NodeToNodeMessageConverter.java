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
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodePropertiesBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import java.util.stream.Collectors;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.NodeProperties.Builder;

public class NodeToNodeMessageConverter implements TwoWayConverter<Node, NodeEntities.Node> {

  public static final NodeToNodeMessageConverter INSTANCE = new NodeToNodeMessageConverter();

  private static final IpAddressMessageToIpAddress IP_ADDRESS_CONVERTER = new IpAddressMessageToIpAddress();
  private static final NodeTypeToNodeTypeMessage NODE_TYPE_CONVERTER = new NodeTypeToNodeTypeMessage();

  private static final NodePropertiesMessageToNodePropertiesConverter NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();
  private static final LoginCredentialMessageToLoginCredentialConverter LOGIN_CREDENTIAL_CONVERTER = LoginCredentialMessageToLoginCredentialConverter.INSTANCE;

  private static final NodeStateConverter NODE_STATE_CONVERTER = new NodeStateConverter();

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
        .setState(NODE_STATE_CONVERTER.apply(node.state()));

    if (node.loginCredential().isPresent()) {
      builder
          .setLoginCredential(LOGIN_CREDENTIAL_CONVERTER.applyBack(node.loginCredential().get()));
    }

    return builder.build();
  }

  private static class NodePropertiesMessageToNodePropertiesConverter implements
      TwoWayConverter<NodeEntities.NodeProperties, NodeProperties> {

    private final GeoLocationMessageToGeoLocationConverter geoLocationConverter = new GeoLocationMessageToGeoLocationConverter();
    private final OperatingSystemConverter operatingSystemConverter = new OperatingSystemConverter();

    @Override
    public NodeEntities.NodeProperties applyBack(NodeProperties nodeProperties) {
      final Builder builder = NodeEntities.NodeProperties.newBuilder()
          .setProviderId(nodeProperties.providerId())
          .setNumberOfCores(nodeProperties.numberOfCores()).setMemory(nodeProperties.memory());

      if (nodeProperties.geoLocation().isPresent()) {
        builder.setGeoLocation(geoLocationConverter.applyBack(nodeProperties.geoLocation().get()));
      }

      if (nodeProperties.operatingSystem().isPresent()) {
        builder.setOperationSystem(
            operatingSystemConverter.applyBack(nodeProperties.operatingSystem().get()));
      }

      if (nodeProperties.disk().isPresent()) {
        builder.setDisk(nodeProperties.disk().get());
      }

      return builder.build();

    }

    @Override
    public NodeProperties apply(NodeEntities.NodeProperties nodeProperties) {
      return NodePropertiesBuilder.newBuilder().providerId(nodeProperties.getProviderId())
          .numberOfCores(nodeProperties.getNumberOfCores())
          .disk(nodeProperties.getDisk())
          .geoLocation(geoLocationConverter.apply(nodeProperties.getGeoLocation()))
          .memory(nodeProperties.getMemory())
          .os(operatingSystemConverter.apply(nodeProperties.getOperationSystem())).build();
    }
  }

  private static class NodeStateConverter implements
      TwoWayConverter<NodeState, NodeEntities.NodeState> {

    @Override
    public NodeState applyBack(NodeEntities.NodeState nodeState) {
      switch (nodeState) {
        case NODE_STATE_OK:
          return NodeState.OK;
        case NODE_STATE_NEW:
          return NodeState.NEW;
        case NODE_STATE_ERROR:
          return NodeState.ERROR;
        case NODE_STATE_DELETED:
          return NodeState.DELETED;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown nodeState " + nodeState);
      }
    }

    @Override
    public NodeEntities.NodeState apply(NodeState nodeState) {

      switch (nodeState) {
        case NEW:
          return NodeEntities.NodeState.NODE_STATE_NEW;
        case ERROR:
          return NodeEntities.NodeState.NODE_STATE_ERROR;
        case OK:
          return NodeEntities.NodeState.NODE_STATE_OK;
        case DELETED:
          return NodeEntities.NodeState.NODE_STATE_DELETED;
        default:
          throw new AssertionError("Unknown node state " + nodeState);
      }
    }
  }

  private static class NodeTypeToNodeTypeMessage implements
      TwoWayConverter<NodeType, NodeEntities.NodeType> {

    @Override
    public NodeType applyBack(NodeEntities.NodeType nodeType) {
      switch (nodeType) {
        case VM:
          return NodeType.VM;
        case BYON:
          return NodeType.BYON;
        case CONTAINER:
          return NodeType.CONTAINER;
        case FAAS:
          return NodeType.FAAS;
        case UNKNOWN_TYPE:
          return NodeType.UNKOWN;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(String.format("The nodeType %s is not known.", nodeType));
      }
    }

    @Override
    public NodeEntities.NodeType apply(NodeType nodeType) {
      switch (nodeType) {
        case VM:
          return NodeEntities.NodeType.VM;
        case BYON:
          return NodeEntities.NodeType.BYON;
        case UNKOWN:
          return NodeEntities.NodeType.UNKNOWN_TYPE;
        case CONTAINER:
          return NodeEntities.NodeType.CONTAINER;
        case FAAS:
          return NodeEntities.NodeType.FAAS;
        default:
          throw new AssertionError(String.format("The nodeType %s is not known.", nodeType));
      }
    }
  }
}
