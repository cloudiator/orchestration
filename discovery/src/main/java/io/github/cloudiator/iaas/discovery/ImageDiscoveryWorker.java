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
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;
import io.github.cloudiator.persistance.ImageDomainRepository;
import java.util.function.Predicate;

/**
 * Created by daniel on 01.06.17.
 */
public class ImageDiscoveryWorker extends AbstractDiscoveryWorker<Image> {

  private final ImageDomainRepository imageDomainRepository;
  private final DiscoveryService discoveryService;

  @Inject
  public ImageDiscoveryWorker(DiscoveryQueue discoveryQueue,
      DiscoveryService discoveryService, DiscoveryErrorHandler discoveryErrorHandler,
      ImageDomainRepository imageDomainRepository) {
    super(discoveryQueue, discoveryErrorHandler);
    this.discoveryService = discoveryService;
    this.imageDomainRepository = imageDomainRepository;
  }

  @Override
  protected Iterable<Image> resources() {
    return discoveryService.listImages();
  }

  @Override
  protected Predicate<Image> filter() {
    return new Predicate<Image>() {
      @Override
      public boolean test(Image image) {
        return imageDomainRepository.findById(image.id()) == null;
      }
    };
  }
}
