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

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
class NodePropertiesModel extends Model {

  @Column(nullable = false)
  private String providerId;

  @Nullable
  private Integer numberOfCores;

  @Nullable
  private Long memory;

  @Column(nullable = true)
  @Nullable
  private Double disk;

  @OneToOne(optional = true)
  @Nullable
  private OperatingSystemModel operatingSystem;

  @OneToOne(optional = true)
  @Nullable
  private GeoLocationModel geoLocation;

  /**
   * Empty constructor for hibernate.
   */
  protected NodePropertiesModel() {

  }

  NodePropertiesModel(String providerId, @Nullable Integer numberOfCores, @Nullable Long memory,
      @Nullable Double disk,
      @Nullable OperatingSystemModel operatingSystem, @Nullable GeoLocationModel geoLocation) {

    this.providerId = providerId;
    this.numberOfCores = numberOfCores;
    this.memory = memory;
    this.disk = disk;
    this.operatingSystem = operatingSystem;
    this.geoLocation = geoLocation;

  }

  @Nullable
  public Integer getNumberOfCores() {
    return numberOfCores;
  }

  @Nullable
  public Long getMemory() {
    return memory;
  }

  @Nullable
  public Double getDisk() {
    return disk;
  }

  @Nullable
  public OperatingSystemModel getOperatingSystem() {
    return operatingSystem;
  }

  @Nullable
  public GeoLocationModel getGeoLocation() {
    return geoLocation;
  }

  public String getProviderId() {
    return providerId;
  }
}
