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

import static com.google.common.base.Preconditions.checkState;

import io.github.cloudiator.domain.Node;
import java.util.Set;
import javax.inject.Inject;
import org.cloudiator.iaas.node.check.NodeCheck;
import org.cloudiator.iaas.node.check.NodeReplacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeNodeSchedulingStrategy implements NodeSchedulingStrategy {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeNodeSchedulingStrategy.class);
  private final Set<NodeSchedulingStrategy> strategies;
  private final NodeReplacement nodeReplacement;

  @Inject
  public CompositeNodeSchedulingStrategy(
      Set<NodeSchedulingStrategy> strategies,
      NodeReplacement nodeReplacement) {
    this.strategies = strategies;
    this.nodeReplacement = nodeReplacement;
  }

  @Override
  public boolean canSchedule(Node pending) {

    for (NodeSchedulingStrategy nodeSchedulingStrategy : strategies) {
      if (nodeSchedulingStrategy.canSchedule(pending)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Node schedule(Node pending) throws NodeSchedulingException {

    checkState(canSchedule(pending), "Can not schedule " + pending);

    for (NodeSchedulingStrategy nodeSchedulingStrategy : strategies) {
      if (nodeSchedulingStrategy.canSchedule(pending)) {

        Node allocated = execute(nodeSchedulingStrategy, pending);

        int current = 1;
        int maxTries = 2;

        while (!checkNode(allocated) && current <= maxTries) {
          LOGGER.warn(String.format("Node %s failed node check, starting replacement.", allocated));
          allocated = nodeReplacement.forNode(allocated).execute();
          current++;
        }

        return allocated;
      }
    }

    throw new IllegalStateException(
        String.format("Could not find a strategy to schedule %s. Tried %s.", pending, strategies));
  }

  public Node execute(NodeSchedulingStrategy nodeSchedulingStrategy, Node pending)
      throws NodeSchedulingException {
    return nodeSchedulingStrategy.schedule(pending);
  }

  private boolean checkNode(Node node) {
    return NodeCheck.forNode(node).execute();
  }
}
