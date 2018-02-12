/*
 * Copyright (c) 2014-2017 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * @todo we should probably normalize this table?
 */
@Entity
class GeoLocationModel extends Model {

  private String city;

  private String country;

  private BigDecimal locationLatitude;

  private BigDecimal locationLongitude;

  @OneToOne(mappedBy = "geoLocationModel")
  private LocationModel locationModel;

  /**
   * No-args constructor used by hibernate.
   */
  protected GeoLocationModel() {
  }

  public GeoLocationModel(String city, String country,
      BigDecimal locationLatitude, BigDecimal locationLongitude) {
    this.city = city;
    this.country = country;
    this.locationLatitude = locationLatitude;
    this.locationLongitude = locationLongitude;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

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
