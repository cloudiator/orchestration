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

package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import io.github.cloudiator.messaging.LocationMessageToLocationConverter;
import io.github.cloudiator.persistance.LocationDomainRepository;
import java.util.stream.Collectors;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messages.Location.LocationQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocationQueryRequest.class);
  private static final LocationMessageToLocationConverter LOCATION_CONVERTER = LocationMessageToLocationConverter.INSTANCE;
  private final MessageInterface messageInterface;
  private final LocationDomainRepository locationDomainRepository;

  @Inject
  public LocationQuerySubscriber(MessageInterface messageInterface,
      LocationDomainRepository locationDomainRepository) {
    this.messageInterface = messageInterface;
    this.locationDomainRepository = locationDomainRepository;
  }


  @Override
  public void run() {
    messageInterface.subscribe(LocationQueryRequest.class, LocationQueryRequest.parser(),
        (requestId, locationQueryRequest) -> {

          try {
            decideAndReply(requestId, locationQueryRequest);
          } catch (Exception e) {
            LOGGER.error(String
                .format("Caught exception %s during execution of %s", e.getMessage(), this), e);
          }

        });
  }

  private void decideAndReply(String requestId, LocationQueryRequest request) {
    if (request.getUserId().isEmpty()) {
      replyErrorNoUserId(requestId);
      return;
    }
    if (!request.getLocationId().isEmpty()) {
      replyForUserIdAndLocationId(requestId, request.getUserId(), request.getLocationId());
      return;
    }
    if (!request.getCloudId().isEmpty()) {
      replyForUserIdAndCloudId(requestId, request.getUserId(), request.getCloudId());
      return;
    }
    replyForUserId(requestId, request.getUserId());
  }


  private void replyErrorNoUserId(String requestId) {
    messageInterface.reply(LocationQueryResponse.class, requestId,
        Error.newBuilder().setCode(500).setMessage("Request does not contain userId.")
            .build());
  }


  private void replyForUserIdAndLocationId(String requestId, String userId, String locationId) {
    final Location location = locationDomainRepository
        .findByTenantAndId(userId, locationId);
    if (location == null) {
      messageInterface.reply(requestId, LocationQueryResponse.newBuilder().build());
    } else {
      LocationQueryResponse locationQueryResponse = LocationQueryResponse.newBuilder()
          .addLocations(LOCATION_CONVERTER.applyBack(location)).build();
      messageInterface.reply(requestId, locationQueryResponse);
    }

  }

  private void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    LocationQueryResponse locationQueryResponse = LocationQueryResponse.newBuilder()
        .addAllLocations(
            locationDomainRepository.findByTenantAndCloud(userId, cloudId).stream().map(
                LOCATION_CONVERTER::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, locationQueryResponse);
  }

  private void replyForUserId(String requestId, String userId) {
    LocationQueryResponse locationQueryResponse = LocationQueryResponse.newBuilder()
        .addAllLocations(locationDomainRepository.findAll(userId).stream().map(
            LOCATION_CONVERTER::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, locationQueryResponse);
  }
}
