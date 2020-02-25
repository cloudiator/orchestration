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

package io.github.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;

public class NodeBuilder extends AbstractNodeBuilder<NodeBuilder> {

  private NodeState state;
  private String originId;

  private NodeBuilder() {
    super();
  }

  private NodeBuilder(Node node) {
    super(node);
    state = node.state();
    originId = node.originId().orElse(null);
  }

  private NodeBuilder(VirtualMachine virtualMachine, NodeType nodeType) {
    super(virtualMachine, nodeType);
    originId = virtualMachine.id();
  }

  private NodeBuilder(VirtualMachine virtualMachine) {
    this(virtualMachine, NodeType.VM);
  }

  public static NodeBuilder newBuilder() {
    return new NodeBuilder();
  }

  public static NodeBuilder of(Node node) {
    checkNotNull(node, "node is null");
    return new NodeBuilder(node);
  }

  public static NodeBuilder of(VirtualMachine virtualMachine, NodeType nodeType) {
    return new NodeBuilder(virtualMachine, nodeType);
  }

  public static NodeBuilder of(VirtualMachine virtualMachine) {
    return new NodeBuilder(virtualMachine);
  }

  public NodeBuilder state(NodeState state) {
    this.state = state;
    return this;
  }

  public NodeBuilder originId(String originId) {
    this.originId = originId;
    return this;
  }

  @Override
  protected NodeBuilder self() {
    return this;
  }

  @Override
  public NodeBuilder nodeType(NodeType nodeType) {
    this.nodeType = nodeType;
    return this;
  }

  @Override
  public Node build() {
    return new NodeImpl(nodeProperties, loginCredential, nodeType, ipAddresses, id, name, state,
        userId, diagnostic, reason, nodeCandidate, originId);
  }
}
