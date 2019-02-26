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

import java.math.BigDecimal;
import javax.annotation.Nullable;
import javax.persistence.Entity;

/**
 * @todo we should probably normalize this table?
 */
@Entity
class GeoLocationModel extends Model {

  @Nullable
  private String city;

  @Nullable
  private String country;

  @Nullable
  private BigDecimal locationLatitude;

  @Nullable
  private BigDecimal locationLongitude;

  /**
   * No-args constructor used by hibernate.
   */
  protected GeoLocationModel() {
  }

  public GeoLocationModel(@Nullable String city, @Nullable String country,
      @Nullable BigDecimal locationLatitude, @Nullable BigDecimal locationLongitude) {
    this.city = city;
    this.country = country;
    this.locationLatitude = locationLatitude;
    this.locationLongitude = locationLongitude;
  }

  @Nullable
  public String getCity() {
    return city;
  }

  public GeoLocationModel setCity(@Nullable String city) {
    this.city = city;
    return this;
  }

  @Nullable
  public String getCountry() {
    return country;
  }

  public GeoLocationModel setCountry(@Nullable String country) {
    this.country = country;
    return this;
  }

  @Nullable
  public BigDecimal getLocationLatitude() {
    return locationLatitude;
  }

  public GeoLocationModel setLocationLatitude(BigDecimal locationLatitude) {
    this.locationLatitude = locationLatitude;
    return this;
  }

  public BigDecimal getLocationLongitude() {
    return locationLongitude;
  }

  public GeoLocationModel setLocationLongitude(BigDecimal locationLongitude) {
    this.locationLongitude = locationLongitude;
    return this;
  }
}
