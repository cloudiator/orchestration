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

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

public class NodePropertiesImpl implements NodeProperties {

  private final String providerId;
  @Nullable
  private final Integer numberOfCores;
  @Nullable
  private final Long memory;
  @Nullable
  private final Double disk;
  @Nullable
  private final OperatingSystem operatingSystem;
  @Nullable
  private final GeoLocation geoLocation;

  NodePropertiesImpl(String providerId, @Nullable Integer numberOfCores, @Nullable Long memory,
      @Nullable Double disk,
      @Nullable OperatingSystem operatingSystem,
      @Nullable GeoLocation geoLocation) {
    this.providerId = providerId;
    this.numberOfCores = numberOfCores;
    this.memory = memory;
    this.disk = disk;
    this.operatingSystem = operatingSystem;
    this.geoLocation = geoLocation;
  }

  @Override
  public String providerId() {
    return providerId;
  }

  @Override
  public Optional<Integer> numberOfCores() {
    return Optional.ofNullable(numberOfCores);
  }

  @Override
  public Optional<Long> memory() {
    return Optional.ofNullable(memory);
  }

  @Override
  public Optional<Double> disk() {
    return Optional.ofNullable(disk);
  }

  @Override
  public Optional<OperatingSystem> operatingSystem() {

    return Optional.ofNullable(operatingSystem);
  }

  @Override
  public Optional<GeoLocation> geoLocation() {
    return Optional.ofNullable(geoLocation);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeProperties that = (NodeProperties) o;
    return Objects.equals(providerId, that.providerId()) &&
        Objects.equals(numberOfCores, that.numberOfCores()) &&
        Objects.equals(memory, that.memory()) &&
        Objects.equals(disk, that.disk()) &&
        Objects.equals(operatingSystem, that.operatingSystem()) &&
        Objects.equals(geoLocation, that.geoLocation());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("providerId", providerId)
        .add("numberOfCores", numberOfCores).add("memory", memory).add("disk", disk)
        .add("os", operatingSystem).add("geoLocation", geoLocation).toString();
  }
}
