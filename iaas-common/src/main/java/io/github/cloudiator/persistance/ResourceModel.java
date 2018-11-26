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
import javax.persistence.Inheritance;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * Created by daniel on 12.05.15.
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.TABLE_PER_CLASS)
abstract class ResourceModel extends Model {


  @Column(nullable = false, updatable = false)
  @Lob
  private String cloudUniqueId;

  @Column(nullable = false, updatable = false)
  private String providerId;

  @Column(nullable = false, updatable = false)
  private String name;

  @ManyToOne(optional = false)
  private CloudModel cloudModel;

  @Nullable
  @ManyToOne(optional = true)
  private LocationModel locationModel;

  protected ResourceModel() {

  }

  public ResourceModel(String cloudUniqueId, String providerId, String name, CloudModel cloudModel,
      @Nullable LocationModel locationModel) {
    this.cloudUniqueId = cloudUniqueId;
    this.providerId = providerId;
    this.name = name;
    this.cloudModel = cloudModel;
    this.locationModel = locationModel;
  }

  public String getCloudUniqueId() {
    return cloudUniqueId;
  }

  public void setCloudUniqueId(String cloudUniqueId) {
    this.cloudUniqueId = cloudUniqueId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public LocationModel getLocationModel() {
    return locationModel;
  }

  public void setLocationModel(@Nullable LocationModel locationModel) {
    this.locationModel = locationModel;
  }

  public CloudModel getCloudModel() {
    return cloudModel;
  }

  public void setCloudModel(CloudModel cloudModel) {
    this.cloudModel = cloudModel;
  }
}
