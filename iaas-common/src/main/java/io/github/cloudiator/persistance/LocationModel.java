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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import de.uniulm.omi.cloudiator.domain.LocationScope;
import io.github.cloudiator.domain.DiscoveryItemState;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
class LocationModel extends Model {

  @Column(nullable = false, updatable = false)
  @Lob
  private String cloudUniqueId;

  @Column(nullable = false, updatable = false)
  private String providerId;

  @Column(updatable = false, nullable = false)
  private String name;

  @ManyToOne(optional = false)
  private CloudModel cloudModel;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DiscoveryItemState state;

  @ManyToOne
  @Nullable
  private LocationModel parent;

  @OneToMany(mappedBy = "parent")
  private List<LocationModel> children;

  @Nullable
  @OneToOne(optional = true)
  @Cascade(CascadeType.DELETE)
  private GeoLocationModel geoLocationModel;

  @Nullable
  @Column(updatable = false)
  @Enumerated(EnumType.STRING)
  private LocationScope
      locationScope;

  @Column(nullable = false, updatable = false)
  private Boolean isAssignable;

  /**
   * Empty constructor for hibernate.
   */
  protected LocationModel() {
  }

  public LocationModel(String cloudUniqueId, String providerId, String name,
      CloudModel cloudModel, @Nullable LocationModel parent,
      @Nullable GeoLocationModel geoLocationModel, @Nullable LocationScope locationScope,
      Boolean isAssignable, DiscoveryItemState state) {
    checkNotNull(cloudUniqueId, "cloudUniqueId is null");
    this.cloudUniqueId = cloudUniqueId;
    checkNotNull(providerId, "providerId is null");
    this.providerId = providerId;
    checkNotNull(name, "name is null");
    this.name = name;
    checkNotNull(cloudModel, "cloudModel is null");
    this.cloudModel = cloudModel;
    this.parent = parent;
    this.geoLocationModel = geoLocationModel;
    this.locationScope = locationScope;
    checkNotNull(isAssignable, "isAssignable is null");
    this.isAssignable = isAssignable;
    checkNotNull(state, "state is null");
    this.state = state;
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
  public LocationModel getParent() {
    return parent;
  }

  public void setParent(@Nullable LocationModel parent) {
    this.parent = parent;
  }

  public List<LocationModel> getChildren() {
    return children;
  }

  @Nullable
  public GeoLocationModel getGeoLocationModel() {
    return geoLocationModel;
  }

  public void setGeoLocationModel(@Nullable GeoLocationModel geoLocationModel) {
    this.geoLocationModel = geoLocationModel;
  }

  @Nullable
  public LocationScope getLocationScope() {
    return locationScope;
  }

  public Boolean getAssignable() {
    return isAssignable;
  }

  public Set<LocationModel> hierachy() {
    final ImmutableSet.Builder<LocationModel> builder = ImmutableSet.builder();
    LocationModel locationModel = this;
    do {
      builder.add(locationModel);
      locationModel = locationModel.getParent();
    } while (locationModel != null);
    return builder.build();
  }

  public CloudModel getCloudModel() {
    return cloudModel;
  }

  public void setCloudModel(CloudModel cloudModel) {
    this.cloudModel = cloudModel;
  }

  public DiscoveryItemState getState() {
    return state;
  }

  public LocationModel setState(DiscoveryItemState state) {
    this.state = state;
    return this;
  }

  public TenantModel getTenant() {
    return getCloudModel().getTenantModel();
  }
}
