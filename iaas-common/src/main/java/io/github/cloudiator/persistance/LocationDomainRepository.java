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

package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.domain.DiscoveredLocation;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class LocationDomainRepository {

  private static final LocationConverter LOCATION_CONVERTER = new LocationConverter();
  private final LocationModelRepository locationModelRepository;
  private final CloudDomainRepository cloudDomainRepository;
  private final GeoLocationDomainRepository geoLocationDomainRepository;

  @Inject
  public LocationDomainRepository(
      LocationModelRepository locationModelRepository,
      CloudDomainRepository cloudDomainRepository,
      GeoLocationDomainRepository geoLocationDomainRepository) {
    this.locationModelRepository = locationModelRepository;
    this.cloudDomainRepository = cloudDomainRepository;
    this.geoLocationDomainRepository = geoLocationDomainRepository;
  }


  public DiscoveredLocation findById(String id) {
    return LOCATION_CONVERTER.apply(locationModelRepository.findByCloudUniqueId(id));
  }

  public DiscoveredLocation findByTenantAndId(String userId, String locationId) {
    return LOCATION_CONVERTER
        .apply(locationModelRepository.findByCloudUniqueIdAndTenant(userId, locationId));
  }

  public List<DiscoveredLocation> findByTenantAndCloud(String tenantId, String cloudId) {
    return locationModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(LOCATION_CONVERTER::apply).collect(Collectors.toList());
  }

  private CloudModel getCloudModel(String id) {
    final String cloudId = IdScopedByClouds.from(id).cloudId();
    return cloudDomainRepository.findModelById(cloudId);
  }

  LocationModel saveAndGet(DiscoveredLocation domain) {
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

  LocationModel getModel(Location location) {
    return locationModelRepository.findByCloudUniqueId(location.id());
  }

  public void save(DiscoveredLocation domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }


  void update(DiscoveredLocation domain, LocationModel model) {
    updateModel(domain, model);
    locationModelRepository.save(model);
  }

  private LocationModel createModel(DiscoveredLocation domain) {
    final CloudModel cloudModel = getCloudModel(domain.id());
    checkState(cloudModel != null, String
        .format("Can not save location %s as related cloudModel is missing.",
            domain));

    LocationModel parent = null;
    //save the parent location
    if (domain.parent().isPresent()) {
      parent = getModel(domain.parent().get());
      if (parent == null) {
        throw new MissingLocationException(String
            .format("Parent location %s is currently missing. Can not persist the location %s.",
                domain.parent().get(), domain));
      }
    }

    GeoLocationModel geoLocationModel = null;
    if (domain.geoLocation().isPresent()) {
      geoLocationModel = geoLocationDomainRepository
          .saveAndGet(domain.geoLocation().get());
    }

    return new LocationModel(
        domain.id(), domain.providerId(), domain.name(), cloudModel, parent,
        geoLocationModel,
        domain.locationScope(), domain.isAssignable(), domain.state());
  }

  private void updateModel(DiscoveredLocation domain, LocationModel model) {

    //check if id matches
    checkState(domain.id().equals(model.getCloudUniqueId()), "ids do not match");

    //updated the state
    model.setState(domain.state());

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


  public Collection<DiscoveredLocation> findAll(String userId) {
    checkNotNull(userId, "userId is null");
    return locationModelRepository.findByTenant(userId).stream().map(LOCATION_CONVERTER::apply)
        .collect(Collectors.toList());
  }
}
