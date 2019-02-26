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

import io.github.cloudiator.domain.LocalVirtualMachineState;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Created by daniel on 31.10.14.
 */
@Entity
class VirtualMachineModel extends Model {

  @Column(nullable = false, updatable = false)
  @Lob
  private String cloudUniqueId;

  @Column(nullable = false, updatable = false)
  @Lob
  private String providerId;

  @Column(nullable = false, updatable = false)
  @Lob
  private String name;

  @Column(nullable = false)
  @Lob
  private String cloudId;

  @ManyToOne(optional = false)
  private TenantModel tenantModel;

  @Nullable
  @Lob
  private String locationId;

  @OneToOne(cascade = CascadeType.ALL)
  @Nullable
  private LoginCredentialModel loginCredential;

  @Nullable
  @Lob
  private String imageId;

  @Nullable
  @Lob
  private String hardwareId;

  @OneToOne
  @Nullable
  private IpGroupModel ipGroup;

  @Column(nullable = false)
  private LocalVirtualMachineState state;

  /**
   * Empty constructor for hibernate.
   */
  protected VirtualMachineModel() {
  }

  public VirtualMachineModel(String cloudUniqueId, String providerId, String name,
      String cloudId,
      TenantModel tenantModel,
      @Nullable String locationId,
      @Nullable LoginCredentialModel loginCredential,
      @Nullable String imageId,
      @Nullable String hardwareId, @Nullable IpGroupModel ipGroup, LocalVirtualMachineState state) {

    checkNotNull(cloudUniqueId, "cloudUniqueId is null");
    this.cloudUniqueId = cloudUniqueId;
    checkNotNull(providerId, "providerId is null");
    this.providerId = providerId;
    checkNotNull(name, "name is null");
    this.name = name;
    checkNotNull(cloudId, "cloudId is null");
    this.cloudId = cloudId;
    checkNotNull(tenantModel, "tenantModel is null");
    this.tenantModel = tenantModel;
    this.locationId = locationId;
    this.loginCredential = loginCredential;
    this.imageId = imageId;
    this.hardwareId = hardwareId;
    this.ipGroup = ipGroup;
    this.state = state;
  }


  @Nullable
  public LoginCredentialModel getLoginCredential() {
    return loginCredential;
  }

  @Nullable
  public IpGroupModel getIpGroup() {
    return ipGroup;
  }

  public Set<IpAddressModel> ipAddresses() {
    if (ipGroup == null) {
      return Collections.emptySet();
    }
    return ipGroup.getIpAddresses();
  }

  public String getCloudUniqueId() {
    return cloudUniqueId;
  }

  public String getProviderId() {
    return providerId;
  }

  public String getName() {
    return name;
  }

  public String getCloudId() {
    return cloudId;
  }

  @Nullable
  public String getLocationId() {
    return locationId;
  }

  @Nullable
  public String getImageId() {
    return imageId;
  }

  @Nullable
  public String getHardwareId() {
    return hardwareId;
  }

  public TenantModel getTenantModel() {
    return tenantModel;
  }

  public LocalVirtualMachineState getState() {
    return state;
  }

  public VirtualMachineModel setState(LocalVirtualMachineState state) {
    this.state = state;
    return this;
  }
}
