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
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import io.github.cloudiator.domain.DiscoveredHardware;
import io.github.cloudiator.domain.DiscoveredImage;
import io.github.cloudiator.domain.DiscoveryItemState;
import io.github.cloudiator.persistance.ImageDomainRepository;
import io.github.cloudiator.persistance.MissingLocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class ImageDiscoveryListener implements DiscoveryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageDiscoveryListener.class);
  private final ImageDomainRepository imageDomainRepository;

  @Inject
  public ImageDiscoveryListener(
      ImageDomainRepository imageDomainRepository) {
    this.imageDomainRepository = imageDomainRepository;
  }

  @Override
  public Class<?> interestedIn() {
    return Image.class;
  }

  @Override
  @Transactional
  public void handle(Object o) {

    final Image image = (Image) o;

    final DiscoveredImage byId = imageDomainRepository.findById(image.id());

    if (byId != null) {
      LOGGER.trace(String.format("Skipping image %s. Already exists.", image));
      return;
    }

    DiscoveredImage discoveredImage = new DiscoveredImage(image, DiscoveryItemState.NEW);

    try {
      imageDomainRepository.save(discoveredImage);
    } catch (MissingLocationException e) {
      LOGGER.trace("Skipping discovery of image %s as assigned location seems to be missing.", e);
    }
  }
}
