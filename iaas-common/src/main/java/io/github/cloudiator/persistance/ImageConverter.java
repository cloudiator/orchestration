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

import de.uniulm.omi.cloudiator.sword.domain.ImageBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.DiscoveredImage;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
class ImageConverter implements OneWayConverter<ImageModel, DiscoveredImage> {

  private final LocationConverter locationConverter = new LocationConverter();

  @Nullable
  @Override
  public DiscoveredImage apply(@Nullable ImageModel imageModel) {
    if (imageModel == null) {
      return null;
    }
    return new DiscoveredImage(ImageBuilder.newBuilder().os(imageModel.operatingSystem())
        .location(locationConverter.apply(imageModel.getLocationModel()))
        .providerId(imageModel.getProviderId()).id(imageModel.getCloudUniqueId())
        .name(imageModel.getName()).build(), imageModel.getState(),
        imageModel.getTenant().getUserId());
  }
}
