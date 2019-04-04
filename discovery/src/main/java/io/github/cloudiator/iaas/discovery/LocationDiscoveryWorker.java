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
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;
import io.github.cloudiator.persistance.LocationDomainRepository;
import java.util.function.Predicate;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationDiscoveryWorker extends AbstractDiscoveryWorker<Location> {

  private final LocationDomainRepository locationDomainRepository;

  @Inject
  public LocationDiscoveryWorker(DiscoveryQueue discoveryQueue,
      DiscoveryService discoveryService, DiscoveryErrorHandler discoveryErrorHandler,
      LocationDomainRepository locationDomainRepository) {
    super(discoveryQueue, discoveryService, discoveryErrorHandler);
    this.locationDomainRepository = locationDomainRepository;
  }

  @Override
  protected Iterable<Location> resources(DiscoveryService discoveryService) {
    return discoveryService.listLocations();
  }

  @Override
  protected Predicate<Location> filter() {
    return new Predicate<Location>() {
      @Override
      public boolean test(Location location) {
        return locationDomainRepository.findById(location.id()) == null;
      }
    };
  }
}
