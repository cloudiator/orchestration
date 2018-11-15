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

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CompositeNodeCandidateIncarnationStrategy implements NodeCandidateIncarnationStrategy {

  private final Set<NodeCandidateIncarnationFactory> factories;
  private final String groupName;
  private final String userId;
  public CompositeNodeCandidateIncarnationStrategy(String groupName, String userId,
      Set<NodeCandidateIncarnationFactory> factories) {
    this.groupName = groupName;
    this.userId = userId;
    this.factories = factories;
  }

  @Override
  public Node apply(NodeCandidate nodeCandidate) throws ExecutionException {

    for (NodeCandidateIncarnationFactory factory :
        factories) {
      if (factory.canIncarnate(nodeCandidate)) {
        return factory.create(groupName, userId).apply(nodeCandidate);
      }
    }

    throw new AssertionError(
        String.format("None of the factories (%s) supports the node candidate %s.",
            Joiner.on(",").join(factories), nodeCandidate));
  }

  public static class CompositeNodeCandidateIncarnationFactory implements
      NodeCandidateIncarnationFactory {

    private final Set<NodeCandidateIncarnationFactory> factories;

    @Inject
    public CompositeNodeCandidateIncarnationFactory(
        Set<NodeCandidateIncarnationFactory> factories) {
      this.factories = factories;
    }


    @Override
    public boolean canIncarnate(NodeCandidate nodeCandidate) {
      //todo: check
      return true;
    }

    @Override
    public NodeCandidateIncarnationStrategy create(String groupName, String userId) {
      return new CompositeNodeCandidateIncarnationStrategy(groupName, userId, factories);
    }
  }
}
