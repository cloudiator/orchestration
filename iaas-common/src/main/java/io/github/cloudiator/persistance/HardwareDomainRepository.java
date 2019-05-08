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

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.domain.DiscoveredHardware;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
public class HardwareDomainRepository {

  private static final HardwareConverter HARDWARE_CONVERTER = new HardwareConverter();
  private final ResourceRepository<HardwareModel> hardwareModelRepository;
  private final CloudDomainRepository cloudDomainRepository;
  private final LocationDomainRepository locationDomainRepository;
  private final HardwareOfferModelRepository hardwareOfferModelRepository;
  private final LocationModelRepository locationModelRepository;

  @Inject
  public HardwareDomainRepository(
      ResourceRepository<HardwareModel> hardwareModelRepository,
      CloudDomainRepository cloudDomainRepository,
      LocationDomainRepository locationDomainRepository,
      HardwareOfferModelRepository hardwareOfferModelRepository,
      LocationModelRepository locationModelRepository) {
    this.hardwareModelRepository = hardwareModelRepository;
    this.cloudDomainRepository = cloudDomainRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.hardwareOfferModelRepository = hardwareOfferModelRepository;
    this.locationModelRepository = locationModelRepository;
  }


  public DiscoveredHardware findById(String id) {
    return HARDWARE_CONVERTER.apply(hardwareModelRepository.findByCloudUniqueId(id));
  }

  public DiscoveredHardware findByTenantAndId(String userId, String hardwareId) {
    return HARDWARE_CONVERTER
        .apply(hardwareModelRepository.findByCloudUniqueIdAndTenant(userId, hardwareId));
  }

  public List<DiscoveredHardware> findByTenantAndCloud(String tenantId, String cloudId) {
    return hardwareModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(HARDWARE_CONVERTER::apply).collect(Collectors.toList());
  }

  public void save(DiscoveredHardware domain) {
    saveAndGet(domain);
  }

  HardwareModel saveAndGet(DiscoveredHardware domain) {

    HardwareModel hardwareModel = hardwareModelRepository.findByCloudUniqueId(domain.id());

    if (hardwareModel == null) {
      hardwareModel = createModel(domain);
    } else {
      updateModel(domain, hardwareModel);
    }

    hardwareModelRepository.save(hardwareModel);
    return hardwareModel;
  }

  private HardwareModel createModel(DiscoveredHardware domain) {

    //get corresponding cloudModel
    final CloudModel cloudModel = getCloudModel(domain.id());
    checkState(cloudModel != null, String
        .format("Can not save hardwareFlavor %s as related cloudModel is missing.",
            domain));

    final HardwareOfferModel hardwareOfferModel = getOrCreateHardwareOffer(domain);

    final LocationModel locationModel = getLocationModel(domain);

    return new HardwareModel(domain.id(), domain.providerId(),
        domain.name(), cloudModel, locationModel, hardwareOfferModel, domain.state());
  }

  private void updateModel(DiscoveredHardware domain, HardwareModel model) {

    checkState(domain.id().equals(model.getCloudUniqueId()), "ids do not match");

    model.setState(domain.state());

    model.setHardwareOfferModel(getOrCreateHardwareOffer(domain));
  }

  private CloudModel getCloudModel(String id) {
    final String cloudId = IdScopedByClouds.from(id).cloudId();
    return cloudDomainRepository.findModelById(cloudId);
  }

  private HardwareOfferModel getOrCreateHardwareOffer(DiscoveredHardware domain) {
    //generate hardware offer
    HardwareOfferModel hardwareOfferModel = hardwareOfferModelRepository
        .findByCpuRamDisk(domain.numberOfCores(), domain.mbRam(),
            domain.gbDisk().orElse(null));
    if (hardwareOfferModel == null) {
      hardwareOfferModel = new HardwareOfferModel(domain.numberOfCores(),
          domain.mbRam(),
          domain.gbDisk().orElse(null));
    }
    hardwareOfferModelRepository.save(hardwareOfferModel);
    return hardwareOfferModel;
  }


  @Nullable
  private LocationModel getLocationModel(DiscoveredHardware domain) {

    if (!domain.location().isPresent()) {
      return null;
    }

    final LocationModel model = locationDomainRepository.getModel(domain.location().get());

    if (model == null) {
      throw new MissingLocationException(
          "Location with id %s is missing. Can not persist the image");
    }

    return model;
  }

  public void delete(DiscoveredHardware hardwareFlavor) {
    final HardwareModel byCloudUniqueId = this.hardwareModelRepository
        .findByCloudUniqueId(hardwareFlavor.id());
    this.hardwareModelRepository.delete(byCloudUniqueId);
  }

  public List<DiscoveredHardware> findAll() {
    return hardwareModelRepository.findAll().stream().map(
        HARDWARE_CONVERTER::apply).collect(Collectors.toList());
  }

  public List<DiscoveredHardware> findAll(String user) {
    return hardwareModelRepository.findByTenant(user).stream().map(
        HARDWARE_CONVERTER::apply).collect(Collectors.toList());
  }

}
