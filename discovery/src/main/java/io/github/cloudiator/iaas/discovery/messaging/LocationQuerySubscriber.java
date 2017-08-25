package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import io.github.cloudiator.iaas.common.messaging.LocationMessageToLocationConverter;
import io.github.cloudiator.iaas.common.persistance.domain.LocationDomainRepository;
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

  private final MessageInterface messageInterface;
  private final LocationDomainRepository locationDomainRepository;
  private final LocationMessageToLocationConverter locationConverter;
  private static final Logger LOGGER = LoggerFactory.getLogger(LocationQueryRequest.class);

  @Inject
  public LocationQuerySubscriber(MessageInterface messageInterface,
      LocationDomainRepository locationDomainRepository,
      LocationMessageToLocationConverter locationConverter) {
    this.messageInterface = messageInterface;
    this.locationDomainRepository = locationDomainRepository;
    this.locationConverter = locationConverter;
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
      replyForUserIdAndLocationId(requestId, request.getUserId(), request.getCloudId());
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
      messageInterface.reply(LocationQueryResponse.class, requestId,
          Error.newBuilder().setCode(404)
              .setMessage(String.format("Location with id %s was not found.", locationId))
              .build());
    } else {
      LocationQueryResponse locationQueryResponse = LocationQueryResponse.newBuilder()
          .addLocations(locationConverter.applyBack(location)).build();
      messageInterface.reply(requestId, locationQueryResponse);
    }

  }

  private void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    LocationQueryResponse locationQueryResponse = LocationQueryResponse.newBuilder()
        .addAllLocations(
            locationDomainRepository.findByTenantAndCloud(userId, cloudId).stream().map(
                locationConverter::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, locationQueryResponse);
  }

  private void replyForUserId(String requestId, String userId) {
    LocationQueryResponse locationQueryResponse = LocationQueryResponse.newBuilder()
        .addAllLocations(locationDomainRepository.findAll(userId).stream().map(
            locationConverter::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, locationQueryResponse);
  }
}
