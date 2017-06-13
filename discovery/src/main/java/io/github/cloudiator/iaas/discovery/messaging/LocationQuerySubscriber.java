package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import io.github.cloudiator.iaas.common.persistance.domain.LocationDomainRepository;
import io.github.cloudiator.iaas.discovery.converters.LocationMessageToLocationConverter;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messages.Location.LocationQueryResponse;
import org.cloudiator.messaging.MessageInterface;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationQuerySubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final LocationDomainRepository locationDomainRepository;
  private final LocationMessageToLocationConverter locationConverter;

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

          //todo check if user exists?

          List<Location> locations = locationDomainRepository
              .findAll(locationQueryRequest.getUserId());

          final LocationQueryResponse locationQueryResponse = LocationQueryResponse.newBuilder()
              .addAllLocations(locations.stream().map(
                  locationConverter::applyBack).collect(Collectors.toList())).build();

          messageInterface.reply(requestId, locationQueryResponse);
        });
  }
}
