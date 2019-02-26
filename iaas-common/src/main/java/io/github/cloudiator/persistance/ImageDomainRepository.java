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
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.domain.DiscoveredImage;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
public class ImageDomainRepository {

  private static final ImageConverter IMAGE_CONVERTER = new ImageConverter();
  private final ResourceRepository<ImageModel> imageModelRepository;
  private final LocationDomainRepository locationDomainRepository;
  private final CloudDomainRepository cloudDomainRepository;
  private final OperatingSystemDomainRepository operatingSystemDomainRepository;

  @Inject
  public ImageDomainRepository(
      ResourceRepository<ImageModel> imageModelRepository,
      LocationDomainRepository locationDomainRepository,
      CloudDomainRepository cloudDomainRepository,
      OperatingSystemDomainRepository operatingSystemDomainRepository) {
    this.imageModelRepository = imageModelRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.cloudDomainRepository = cloudDomainRepository;
    this.operatingSystemDomainRepository = operatingSystemDomainRepository;
  }


  public DiscoveredImage findById(String id) {
    return IMAGE_CONVERTER.apply(imageModelRepository.findByCloudUniqueId(id));
  }

  public DiscoveredImage findByTenantAndId(String userId, String imageId) {
    return IMAGE_CONVERTER
        .apply(imageModelRepository.findByCloudUniqueIdAndTenant(userId, imageId));
  }

  public List<DiscoveredImage> findByTenantAndCloud(String tenantId, String cloudId) {
    return imageModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(IMAGE_CONVERTER).collect(Collectors.toList());
  }

  public void save(DiscoveredImage domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  ImageModel saveAndGet(DiscoveredImage domain) {
    checkNotNull(domain, "domain is null");

    ImageModel model = imageModelRepository.findByCloudUniqueId(domain.id());
    if (model == null) {
      model = createModel(domain);
    } else {
      updateModel(domain, model);
    }
    imageModelRepository.save(model);
    return model;
  }

  private CloudModel getCloudModel(String id) {
    final String cloudId = IdScopedByClouds.from(id).cloudId();
    return cloudDomainRepository.findModelById(cloudId);
  }

  @Nullable
  private LocationModel getLocationModel(DiscoveredImage domain) {

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

  private ImageModel createModel(DiscoveredImage domain) {
    final CloudModel cloudModel = getCloudModel(domain.id());

    checkState(cloudModel != null, String
        .format("Can not save image %s as related cloudModel is missing.",
            domain));

    LocationModel locationModel = getLocationModel(domain);

    OperatingSystemModel operatingSystemModel = operatingSystemDomainRepository
        .saveAndGet(domain.operatingSystem());

    ImageModel imageModel = new ImageModel(domain.id(), domain.providerId(), domain.name(),
        cloudModel,
        locationModel, null, null, operatingSystemModel, domain.state());

    imageModelRepository.save(imageModel);

    return imageModel;
  }

  private void updateModel(DiscoveredImage domain, ImageModel model) {

    checkState(domain.id().equals(model.getCloudUniqueId()), "ids do not match");

    operatingSystemDomainRepository.update(domain.operatingSystem(), model.operatingSystem());
    model.setState(domain.state());
    imageModelRepository.save(model);
  }

  public Collection<DiscoveredImage> findAll(@Nullable String userId) {
    checkNotNull(userId, "userId is null");
    return imageModelRepository.findByTenant(userId).stream().map(IMAGE_CONVERTER)
        .collect(Collectors.toList());
  }
}
