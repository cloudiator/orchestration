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

package org.cloudiator.iaas.node;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeNodeDeletionStrategy implements NodeDeletionStrategy {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeNodeDeletionStrategy.class);
  private final Set<NodeDeletionStrategy> nodeDeletionStrategies;
  private final ForceNodeDeletionStrategy forceNodeDeletionStrategy;

  @Inject
  public CompositeNodeDeletionStrategy(
      Set<NodeDeletionStrategy> nodeDeletionStrategies,
      ForceNodeDeletionStrategy forceNodeDeletionStrategy) {
    this.nodeDeletionStrategies = nodeDeletionStrategies;
    this.forceNodeDeletionStrategy = forceNodeDeletionStrategy;
  }

  @Override
  public boolean supportsNode(Node node) {
    return true;
  }

  @Override
  public boolean deleteNode(Node node) {

    checkNotNull(node, "node is null");

    try {
      for (NodeDeletionStrategy nodeDeletionStrategy : nodeDeletionStrategies) {
        if (nodeDeletionStrategy.supportsNode(node)) {
          LOGGER.debug(String
              .format("%s is using strategy %s to delete node %s.", this, nodeDeletionStrategy,
                  node));
          return nodeDeletionStrategy.deleteNode(node);
        }
      }
    } catch (Exception e) {
      LOGGER.warn(String.format(
          "Error while applying node deletion strategies to node %s. Skipping to force deletion.",
          node));
    }

    LOGGER.warn(String
        .format("%s did not find a suitable strategy to delete node %s. Force deleting the node.",
            this, node));
    return forceNodeDeletionStrategy.deleteNode(node);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("nodeDeletionStrategies", nodeDeletionStrategies)
        .toString();
  }
}
