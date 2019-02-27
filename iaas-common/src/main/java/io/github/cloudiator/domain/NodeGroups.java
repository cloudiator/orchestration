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

import java.util.Collection;
import java.util.UUID;

public class NodeGroups {

  private NodeGroups() {
    throw new AssertionError("Do not instantiate");
  }

  private static String generateId() {
    return UUID.randomUUID().toString();
  }

  public static NodeGroup of(String userId, Collection<Node> nodes) {
    return new NodeGroupImpl(generateId(), userId, nodes);
  }

  public static NodeGroup of(String id, String userId, Collection<Node> nodes) {
    return new NodeGroupImpl(id, userId, nodes);
  }

  public static NodeGroup ofSingle(Node node) {
    return new NodeGroupImpl(generateId(), node.userId(), node);
  }

  public static NodeGroup ofSingle(String id, Node node) {
    return new NodeGroupImpl(id, node.userId(), node);
  }

}
