/*
 * Copyright (c) 2014-2019 University of Ulm
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

package io.github.cloudiator.messaging;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.util.CollectorsUtil;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestMessage;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.MatchmakingService;

public class NodeCandidateMessageRepository {

  private final static String RESPONSE_ERROR = "Could not retrieve NodeCandidate object(s) due to error %s";
  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = NodeCandidateConverter.INSTANCE;
  private final MatchmakingService matchmakingService;

  @Inject
  public NodeCandidateMessageRepository(MatchmakingService matchmakingService) {
    checkNotNull(matchmakingService, "matchmakingService is null");
    this.matchmakingService = matchmakingService;
  }


  public NodeCandidate getById(String userId, String id) {
    try {
      NodeCandidateRequestMessage msg =
          NodeCandidateRequestMessage.newBuilder().setUserId(userId).setId(id).build();
      final NodeCandidateRequestResponse nodeCandidateRequestResponse = matchmakingService
          .requestNodes(msg);
      return nodeCandidateRequestResponse.getCandidatesList().stream().map(NODE_CANDIDATE_CONVERTER)
          .collect(CollectorsUtil.singletonCollector());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }

  public NodeCandidate getById(String id) {
    return getById(null, id);
  }
}
