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


package io.github.cloudiator.iaas.byon.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.iaas.byon.Constants;
import io.github.cloudiator.messaging.ByonToByonMessageConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Byon.ByonNodeQueryRequest;
import org.cloudiator.messages.Byon.ByonNodeQueryResponse;
import org.cloudiator.messages.entities.ByonEntities;
import org.cloudiator.messages.entities.ByonEntities.QueryFilter;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeQuerySubscriber implements Runnable {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonNodeQuerySubscriber.class);
  private final MessageInterface messageInterface;
  private final ByonNodeDomainRepository domainRepository;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public ByonNodeQuerySubscriber(MessageInterface messageInterface,
      ByonNodeDomainRepository domainRepository) {
    this.messageInterface = messageInterface;
    this.domainRepository = domainRepository;
  }

  @Override
  public void run() {
    subscription = messageInterface.subscribe(ByonNodeQueryRequest.class,
        ByonNodeQueryRequest.parser(),
        (requestId, request) -> {
          try {
            final String userId = request.getUserId();
            ByonEntities.QueryFilter filter = request.getFilter();
            List<ByonNode> nodes = getFilteredNodes(filter, userId);
            ByonNodeQueryResponse byonNodeQueryResponse = ByonNodeQueryResponse.newBuilder()
                .addAllByonNode(nodes.stream().map(ByonToByonMessageConverter.INSTANCE::apply)
                    .collect(Collectors.toList())).build();
            messageInterface.reply(requestId, byonNodeQueryResponse);
          } catch (Exception ex) {
            LOGGER.error("Exception occurred.", ex);
            sendErrorResponse(requestId, "Exception occurred: " + ex.getMessage(), Constants.SERVER_ERROR);
          }
        });
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  List<ByonNode> getFilteredNodes(QueryFilter filter, String userId) {
    List<ByonNode> returnNodes = domainRepository.findByTenant(userId);

    if (returnNodes == null) {
      return new ArrayList<>();
    }

    switch(filter) {
      case ALLOCATED:
          return returnNodes.stream().filter(
              byonNode -> byonNode.allocated() == true).collect(Collectors.toList());
      case UNALLOCATED:
        return returnNodes.stream().filter(
            byonNode -> byonNode.allocated() == false).collect(Collectors.toList());
      default:
        return returnNodes;
    }
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messageInterface.reply(ByonNodeQueryResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
