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

package io.github.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;


public class NodePropertiesBuilder {

  private String providerId;
  private Integer numberOfCores;
  private Long memory;
  private Double disk;
  private OperatingSystem operatingSystem;
  private GeoLocation geoLocation;

  private NodePropertiesBuilder() {
  }

  public static NodePropertiesBuilder of(HardwareFlavor hardwareFlavor, Image image,
      Location location) {

    checkNotNull(hardwareFlavor, "hardwareFlavor is null");
    checkNotNull(image, "image is null");
    checkNotNull(location, "location is null");

    final String providerId = IdScopedByClouds.from(hardwareFlavor.id()).cloudId();

    return newBuilder().providerId(providerId).numberOfCores(hardwareFlavor.numberOfCores())
        .memory(hardwareFlavor.mbRam())
        .disk(hardwareFlavor.gbDisk().orElse(null)).os(image.operatingSystem())
        .geoLocation(location.geoLocation().orElse(null));
  }

  public static NodePropertiesBuilder newBuilder() {
    return new NodePropertiesBuilder();
  }

  public NodePropertiesBuilder numberOfCores(Integer numberOfCores) {
    this.numberOfCores = numberOfCores;
    return this;
  }

  public NodePropertiesBuilder memory(Long memory) {
    this.memory = memory;
    return this;
  }

  public NodePropertiesBuilder disk(Double disk) {
    this.disk = disk;
    return this;
  }

  public NodePropertiesBuilder os(OperatingSystem operatingSystem) {
    this.operatingSystem = operatingSystem;
    return this;
  }

  public NodePropertiesBuilder geoLocation(GeoLocation geoLocation) {
    this.geoLocation = geoLocation;
    return this;
  }

  public NodePropertiesBuilder providerId(String providerId) {
    this.providerId = providerId;
    return this;
  }

  public NodeProperties build() {
    return new NodePropertiesImpl(providerId, numberOfCores, memory, disk, operatingSystem,
        geoLocation);
  }


}
