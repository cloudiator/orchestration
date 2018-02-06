package io.github.cloudiator.iaas.common.persistance.domain;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.iaas.common.persistance.domain.converters.LocationConverter;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.entities.GeoLocationModel;
import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.GeoLocationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class LocationDomainRepository {

  private final LocationModelRepository locationModelRepository;
  private final LocationConverter locationConverter = new LocationConverter();
  private final CloudModelRepository cloudModelRepository;
  private final GeoLocationModelRepository geoLocationModelRepository;

  @Inject
  public LocationDomainRepository(
      LocationModelRepository locationModelRepository,
      CloudModelRepository cloudModelRepository,
      GeoLocationModelRepository geoLocationModelRepository) {
    this.locationModelRepository = locationModelRepository;
    this.cloudModelRepository = cloudModelRepository;
    this.geoLocationModelRepository = geoLocationModelRepository;
  }

  public Location findById(String id) {
    return locationConverter.apply(locationModelRepository.findByCloudUniqueId(id));
  }

  public Location findByTenantAndId(String userId, String locationId) {
    return locationConverter
        .apply(locationModelRepository.findByCloudUniqueIdAndTenant(userId, locationId));
  }

  public List<Location> findByTenantAndCloud(String tenantId, String cloudId) {
    return locationModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(locationConverter).collect(Collectors.toList());
  }

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

    GeoLocationModel geoLocationModel = null;
    if (location.geoLocation().isPresent()) {
      GeoLocation geoLocation = location.geoLocation().get();
      geoLocationModel = new GeoLocationModel(geoLocation.city(), geoLocation.country(),
          geoLocation.latitude(), geoLocation.longitude());
      geoLocationModelRepository.save(geoLocationModel);
    }

    LocationModel locationModel = locationModelRepository.findByCloudUniqueId(location.id());
    //todo update case
    if (locationModel == null) {
      locationModel = new LocationModel(
          location.id(), location.providerId(), location.name(), cloudModel, parent,
          geoLocationModel,
          location.locationScope(), location.isAssignable());
    }
    locationModelRepository.save(locationModel);
  }

  public void delete(Location location) {
    final LocationModel byCloudUniqueId = locationModelRepository
        .findByCloudUniqueId(location.id());
    locationModelRepository.delete(byCloudUniqueId);
  }

  public List<Location> findAll() {
    return locationModelRepository.findAll().stream().map(locationConverter).collect(
        Collectors.toList());
  }

  public List<Location> findAll(String user) {

    List<LocationModel> models = locationModelRepository.findByTenant(user);
    return models.stream().map(
        locationConverter).collect(Collectors.toList());
  }
}
