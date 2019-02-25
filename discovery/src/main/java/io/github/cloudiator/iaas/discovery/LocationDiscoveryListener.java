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

package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.domain.DiscoveredLocation;
import io.github.cloudiator.domain.DiscoveryItemState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.iaas.discovery.state.LocationStateMachine;
import io.github.cloudiator.persistance.CloudDomainRepository;
import io.github.cloudiator.persistance.LocationDomainRepository;
import io.github.cloudiator.persistance.MissingLocationException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationDiscoveryListener implements DiscoveryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocationDiscoveryListener.class);
  private final LocationDomainRepository locationDomainRepository;
  private final LocationStateMachine locationStateMachine;
  private final CloudDomainRepository cloudDomainRepository;

  @Inject
  public LocationDiscoveryListener(
      LocationDomainRepository locationDomainRepository,
      LocationStateMachine locationStateMachine,
      CloudDomainRepository cloudDomainRepository) {
    this.locationDomainRepository = locationDomainRepository;
    this.locationStateMachine = locationStateMachine;
    this.cloudDomainRepository = cloudDomainRepository;
  }

  @Override
  public Class<?> interestedIn() {
    return Location.class;
  }

  @Override
  @Transactional
  public void handle(Object o) {
    final Location location = (Location) o;

    final DiscoveredLocation byId = locationDomainRepository.findById(location.id());

    if (byId != null) {
      LOGGER.trace(String.format("Skipping location %s. Already exists.", location));
      return;
    }

    final ExtendedCloud cloud = cloudDomainRepository
        .findById(IdScopedByClouds.from(location.id()).cloudId());

    if (cloud == null) {
      throw new IllegalStateException(
          String.format("Cloud for location %s is not available", location));
    }

    DiscoveredLocation discoveredLocation = new DiscoveredLocation(location,
        DiscoveryItemState.NEW, cloud.userId());

    try {
      locationDomainRepository.save(discoveredLocation);
      locationStateMachine.apply(discoveredLocation, DiscoveryItemState.OK, new Object[0]);
    } catch (MissingLocationException e) {
      LOGGER.trace("Skipping discovery of location %s as assigned parent seems to be missing.", e);
    }
  }
}
