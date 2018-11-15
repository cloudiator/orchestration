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

import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Set;

public class NodeBuilder {

  private NodeProperties nodeProperties;
  private LoginCredential loginCredential;
  private NodeType nodeType;
  private Set<IpAddress> ipAddresses;
  private String id;
  private String name;

  private NodeBuilder() {
  }

  public static NodeBuilder newBuilder() {
    return new NodeBuilder();
  }

  public NodeBuilder nodeProperties(
      NodeProperties nodeProperties) {
    this.nodeProperties = nodeProperties;
    return this;
  }

  public NodeBuilder loginCredential(
      LoginCredential loginCredential) {
    this.loginCredential = loginCredential;
    return this;
  }

  public NodeBuilder nodeType(NodeType nodeType) {
    this.nodeType = nodeType;
    return this;
  }

  public NodeBuilder ipAddresses(
      Set<IpAddress> ipAddresses) {
    this.ipAddresses = ipAddresses;
    return this;
  }

  public NodeBuilder id(String id) {
    this.id = id;
    return this;
  }

  public NodeBuilder name(String name) {
    this.name = name;
    return this;
  }

  public Node build() {
    return new NodeImpl(nodeProperties, loginCredential, nodeType, ipAddresses, id, name);
  }


}
