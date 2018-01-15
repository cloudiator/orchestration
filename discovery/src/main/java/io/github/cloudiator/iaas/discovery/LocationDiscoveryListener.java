package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import io.github.cloudiator.iaas.common.persistance.domain.LocationDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationDiscoveryListener implements DiscoveryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocationDiscoveryListener.class);
  private final LocationDomainRepository locationDomainRepository;

  @Inject
  public LocationDiscoveryListener(
      LocationDomainRepository locationDomainRepository) {
    this.locationDomainRepository = locationDomainRepository;
  }

  @Override
  public Class<?> interestedIn() {
    return Location.class;
  }

  @Override
  @Transactional
  public void handle(Object o) {
    Location location = (Location) o;

    locationDomainRepository.save(location);
  }
}
