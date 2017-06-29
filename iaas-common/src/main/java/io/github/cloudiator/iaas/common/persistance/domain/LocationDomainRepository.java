package io.github.cloudiator.iaas.common.persistance.domain;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.iaas.common.persistance.domain.converters.LocationConverter;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class LocationDomainRepository implements DomainRepository<Location> {

  private final LocationModelRepository locationModelRepository;
  private final LocationConverter locationConverter = new LocationConverter();
  private final CloudModelRepository cloudModelRepository;

  @Inject
  public LocationDomainRepository(
      LocationModelRepository locationModelRepository,
      CloudModelRepository cloudModelRepository) {
    this.locationModelRepository = locationModelRepository;
    this.cloudModelRepository = cloudModelRepository;
  }

  @Override
  public Location findById(String id) {
    return locationConverter.apply(locationModelRepository.findByCloudUniqueId(id));
  }

  @Override
  public void save(Location location) {
    final String cloudId = IdScopedByClouds.from(location.id()).cloudId();
    CloudModel cloudModel = cloudModelRepository.getByCloudId(cloudId);
    checkState(cloudModel != null, String
        .format("Can not save location %s as related cloudModel with id %s is missing.",
            location, cloudId));

    LocationModel parent = null;
    //save the parent location
    if (location.parent().isPresent()) {
      save(location.parent().get());
      parent = locationModelRepository.findByCloudUniqueId(location.parent().get().id());
      checkState(parent != null);
    }

    LocationModel locationModel = locationModelRepository.findByCloudUniqueId(location.id());
    //todo update case
    if (locationModel == null) {
      locationModel = new LocationModel(
          location.id(), location.providerId(), location.name(), cloudModel, parent, null,
          location.locationScope(), location.isAssignable());
    }
    locationModelRepository.save(locationModel);
  }

  @Override
  public void delete(Location location) {
    final LocationModel byCloudUniqueId = locationModelRepository
        .findByCloudUniqueId(location.id());
    locationModelRepository.delete(byCloudUniqueId);
  }

  @Override
  public List<Location> findAll() {
    return locationModelRepository.findAll().stream().map(locationConverter).collect(
        Collectors.toList());
  }

  public List<Location> findAll(String user) {
    return locationModelRepository.findByTenant(user).stream().map(
        locationConverter).collect(Collectors.toList());
  }
}
