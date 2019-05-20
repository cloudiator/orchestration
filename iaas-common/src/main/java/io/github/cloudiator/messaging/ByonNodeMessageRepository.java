
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

package io.github.cloudiator.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.domain.ByonNode;
import java.util.List;
import javax.annotation.Nullable;
import org.cloudiator.messaging.services.NodeService;

public class ByonNodeMessageRepository implements MessageRepository<ByonNode> {

  /* change hier : ByonService statt NodeService
   */
  private final NodeService nodeService;
  private static final ByonToByonMessageConverter NODE_MESSAGE_CONVERTER = ByonToByonMessageConverter.INSTANCE;

  @Inject
  public ByonNodeMessageRepository(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @Nullable
  @Override
  public ByonNode getById(String userId, String id) {
      throw new IllegalStateException("Single ByonNode can not be queried");
  }

  @Override
  public List<ByonNode> getAll(String userId) {

    if(userId != null) {
      throw new IllegalStateException("ByonNode can not be associated with a user id");
    }

    /* change hier : ByonNodeQueryMessage statt NodeQueryMessage
    final NodeQueryMessage nodeQueryMessage = NodeQueryMessage.newBuilder().setUserId(userId)
        .build();

   change hier : ByonService statt NodeService
    try {
      return nodeService.queryNodes(nodeQueryMessage).getNodesList().stream()
          .map(NODE_MESSAGE_CONVERTER::applyBack).collect(Collectors
              .toList());
    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve nodes.", e);
    }
   */
    return null;
  }
}
