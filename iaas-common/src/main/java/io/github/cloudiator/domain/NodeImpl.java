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

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpAddressType;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpVersion;
import de.uniulm.omi.cloudiator.sword.domain.IpAddressImpl;
import de.uniulm.omi.cloudiator.sword.domain.IpAddresses;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class NodeImpl extends BaseNodeImpl implements Node {

  private final String id;
  private final NodeState nodeState;
  private final String userId;

  public NodeImpl(NodeProperties nodeProperties,
      @Nullable LoginCredential loginCredential, NodeType nodeType,
      Set<IpAddress> ipAddresses, String id, String name, NodeState nodeState,
      String userId, @Nullable String diagnostic, @Nullable String reason,
      @Nullable String nodeCandidate, @Nullable String originId) {
    super(nodeProperties, loginCredential, nodeType, ipAddresses, name,
        diagnostic, reason, nodeCandidate, originId);

    this.id = id;
    this.nodeState = nodeState;
    this.userId = userId;
  }

  @Override
  public String userId() {
    return userId;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public NodeState state() {
    return nodeState;
  }

  @Override
  public String toString() {
    String baseStr = super.toString();
    String headStr = MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("userId", userId)
        .add("state", nodeState)
        .toString();

    return headStr + baseStr;
  }
}
