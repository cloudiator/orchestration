package io.github.cloudiator.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import io.github.cloudiator.util.CollectorsUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.LocationService;

public class LocationMessageRepository implements MessageRepository<Location> {

  private final static String RESPONSE_ERROR = "Could not retrieve hardware flavor object(s) due to error %s";
  private final LocationService locationService;
  private final LocationMessageToLocationConverter converter = new LocationMessageToLocationConverter();

  @Inject
  public LocationMessageRepository(
      LocationService locationService) {
    this.locationService = locationService;
  }

  @Override
  public Location getById(String userId, String id) {
    try {
      return locationService
          .getLocations(
              LocationQueryRequest.newBuilder().setLocationId(id).setUserId(userId).build())
          .getLocationsList().stream().map(converter).collect(CollectorsUtil.singletonCollector());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }

  @Override
  public List<Location> getAll(String userId) {
    try {
      return locationService
          .getLocations(LocationQueryRequest.newBuilder().setUserId(userId).build())
          .getLocationsList().stream().map(converter).collect(
              Collectors.toList());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }
}
