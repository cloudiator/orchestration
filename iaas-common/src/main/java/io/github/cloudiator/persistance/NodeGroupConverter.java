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

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.domain.NodeGroups;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class NodeGroupConverter implements OneWayConverter<NodeGroupModel, NodeGroup> {

  private final NodeConverter nodeConverter = new NodeConverter();

  @Nullable
  @Override
  public NodeGroup apply(@Nullable NodeGroupModel nodeGroupModel) {
    if (nodeGroupModel == null) {
      return null;
    }

    return NodeGroups.of(nodeGroupModel.getDomainId(),
        nodeGroupModel.getNodes().stream().map(nodeConverter).collect(
            Collectors.toList()));
  }
}
