package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class LocationDomainRepository {

  private final LocationModelRepository locationModelRepository;
  private final CloudDomainRepository cloudDomainRepository;
  private final GeoLocationDomainRepository geoLocationDomainRepository;
  private static final LocationConverter LOCATION_CONVERTER = new LocationConverter();

  @Inject
  public LocationDomainRepository(
      LocationModelRepository locationModelRepository,
      CloudDomainRepository cloudDomainRepository,
      GeoLocationDomainRepository geoLocationDomainRepository) {
    this.locationModelRepository = locationModelRepository;
    this.cloudDomainRepository = cloudDomainRepository;
    this.geoLocationDomainRepository = geoLocationDomainRepository;
  }


  public Location findById(String id) {
    return LOCATION_CONVERTER.apply(locationModelRepository.findByCloudUniqueId(id));
  }

  public Location findByTenantAndId(String userId, String locationId) {
    return LOCATION_CONVERTER
        .apply(locationModelRepository.findByCloudUniqueIdAndTenant(userId, locationId));
  }

  public List<Location> findByTenantAndCloud(String tenantId, String cloudId) {
    return locationModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(LOCATION_CONVERTER).collect(Collectors.toList());
  }

  private CloudModel getCloudModel(String id) {
    final String cloudId = IdScopedByClouds.from(id).cloudId();
    return cloudDomainRepository.findModelById(cloudId);
  }

  LocationModel saveAndGet(Location domain) {
    checkNotNull(domain, "domain is null");
    LocationModel model = locationModelRepository.findByCloudUniqueId(domain.id());
    if (model == null) {
      model = createModel(domain);
    } else {
      update(domain, model);
    }
    locationModelRepository.save(model);
    return model;
  }

  public void save(Location domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }


  void update(Location domain, LocationModel model) {
    updateModel(domain, model);
    locationModelRepository.save(model);
  }

  private LocationModel createModel(Location domain) {
    final CloudModel cloudModel = getCloudModel(domain.id());
    checkState(cloudModel != null, String
        .format("Can not save location %s as related cloudModel is missing.",
            domain));

    LocationModel parent = null;
    //save the parent location
    if (domain.parent().isPresent()) {
      parent = saveAndGet(domain.parent().get());
    }

    GeoLocationModel geoLocationModel = null;
    if (domain.geoLocation().isPresent()) {
      geoLocationModel = geoLocationDomainRepository
          .saveAndGet(domain.geoLocation().get());
    }

    return new LocationModel(
        domain.id(), domain.providerId(), domain.name(), cloudModel, parent,
        geoLocationModel,
        domain.locationScope(), domain.isAssignable());
  }

  private void updateModel(Location domain, LocationModel model) {

    //we only allow an update of the geolocation
    //todo throw exception if other attributes are updated?

    //create if not already exists
    if (model.getGeoLocationModel() == null && domain.geoLocation().isPresent()) {
      model.setGeoLocationModel(geoLocationDomainRepository.saveAndGet(domain.geoLocation().get()));
    } else if (!domain.geoLocation().isPresent()) {
      //delete if removed
      model.setGeoLocationModel(null);
    } else {
      //update
      geoLocationDomainRepository.update(domain.geoLocation().get(), model.getGeoLocationModel());
    }

    locationModelRepository.save(model);
  }


  public Collection<Location> findAll(String userId) {
    checkNotNull(userId, "userId is null");
    return locationModelRepository.findByTenant(userId).stream().map(LOCATION_CONVERTER)
        .collect(Collectors.toList());
  }
}
