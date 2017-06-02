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

package io.github.cloudiator.iaas.common.persistance.entities;

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
public abstract class ResourceModel extends Model {


  @Column(nullable = false)
  @Lob
  private String cloudUniqueId;

  @Column(nullable = false)
  private String providerId;

  @Column(nullable = false)
  private String name;

  @ManyToOne(optional = false)
  private Cloud cloud;

  @Nullable
  @ManyToOne
  private LocationModel locationModel;

  protected ResourceModel() {

  }

  public ResourceModel(String cloudUniqueId, String providerId, String name, Cloud cloud,
      @Nullable LocationModel locationModel) {
    this.cloudUniqueId = cloudUniqueId;
    this.providerId = providerId;
    this.name = name;
    this.cloud = cloud;
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

  public Cloud getCloud() {
    return cloud;
  }

  public void setCloud(Cloud cloud) {
    this.cloud = cloud;
  }
}
