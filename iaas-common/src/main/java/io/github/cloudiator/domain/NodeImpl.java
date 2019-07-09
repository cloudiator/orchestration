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

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class NodeImpl extends AbstractNodeImpl implements Node {

  private final NodeState nodeState;
  @Nullable
  private final String originId;

  public NodeImpl(NodeProperties nodeProperties,
      @Nullable LoginCredential loginCredential, NodeType nodeType,
      Set<IpAddress> ipAddresses, String id, String name, NodeState nodeState,
      String userId, @Nullable String diagnostic, @Nullable String reason,
      @Nullable String nodeCandidate, @Nullable String originId) {
    super(nodeProperties, userId, loginCredential, nodeType, ipAddresses, id,
        name, diagnostic, reason, nodeCandidate);

    this.nodeState = nodeState;
    this.originId = originId;
  }

  public NodeState state() {
    return nodeState;
  }

  @Override
  public Optional<String> originId() {
    return Optional.ofNullable(originId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Node that = (Node) o;
    return super.equals(o) &&
        Objects.equals(nodeState, that.state()) &&
        Objects.equals(originId, that.originId());
  }


  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper().add("state", nodeState)
        .add("originId", originId);
  }
}
