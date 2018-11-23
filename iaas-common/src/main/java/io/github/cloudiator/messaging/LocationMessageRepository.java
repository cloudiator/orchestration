package io.github.cloudiator.messaging;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.LocationScope;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.LocationService;

public class LocationMessageRepository implements MessageRepository<Location> {

  private final static String RESPONSE_ERROR = "Could not retrieve hardware flavor object(s) due to error %s";
  private final LocationService locationService;
  private static final LocationMessageToLocationConverter CONVERTER = LocationMessageToLocationConverter.INSTANCE;

  @Inject
  public LocationMessageRepository(
      LocationService locationService) {
    this.locationService = locationService;
  }

  @Override
  public Location getById(String userId, String id) {
    try {
      final List<Location> collect = locationService
          .getLocations(
              LocationQueryRequest.newBuilder().setLocationId(id).setUserId(userId).build())
          .getLocationsList().stream().map(CONVERTER).collect(Collectors.toList());

      checkState(collect.size() <= 1, "Expected unique result.");

      if (collect.isEmpty()) {
        return null;
      }
      return collect.get(0);


    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }

  @Override
  public List<Location> getAll(String userId) {
    try {
      return locationService
          .getLocations(LocationQueryRequest.newBuilder().setUserId(userId).build())
          .getLocationsList().stream().map(CONVERTER).collect(
              Collectors.toList());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }

  public String getRegionName(String userId, String locationId) {
    Location location = getById(userId, locationId);
    checkState(location != null, "Location is null");
    while (true) {
      checkState(location.locationScope() != null, "Location scope is null");
      if (location.locationScope().equals(LocationScope.REGION)) {
        return location.providerId();
      } else {
        checkState(location.parent().isPresent(), "Location is not REGION and doesn't have parent");
        location = location.parent().get();
      }
    }
  }
}
