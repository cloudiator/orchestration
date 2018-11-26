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
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;

public class NodeGroupImpl implements NodeGroup {

  private final List<Node> nodes;
  private final String id;

  NodeGroupImpl(String id, Collection<Node> nodes) {
    checkNotNull(id, "id is null");
    checkNotNull(nodes, "nodes is null");
    this.nodes = ImmutableList.copyOf(nodes);
    this.id = id;
  }

  NodeGroupImpl(String id, Node node) {
    checkNotNull(id, "id is null");
    checkNotNull(node, "node is null");
    this.nodes = ImmutableList.of(node);
    this.id = id;
  }

  @Override
  public List<Node> getNodes() {
    return nodes;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("nodes", Joiner.on(",").join(nodes))
        .toString();
  }
}
