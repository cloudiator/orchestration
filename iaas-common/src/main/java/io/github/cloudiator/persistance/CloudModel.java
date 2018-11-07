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

import de.uniulm.omi.cloudiator.sword.domain.CloudType;
import io.github.cloudiator.domain.CloudState;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
class CloudModel extends Model {

  @Column(unique = true, nullable = false, updatable = false)
  private String cloudId;
  @ManyToOne(optional = false)
  private TenantModel tenantModel;

  @ManyToOne(optional = false)
  private ApiModel apiModel;

  @Nullable
  private String endpoint;

  @OneToOne(optional = false, orphanRemoval = true)
  @Cascade(CascadeType.DELETE)
  private CloudCredentialModel cloudCredential;

  @OneToOne(optional = false, orphanRemoval = true)
  @Cascade(CascadeType.DELETE)
  private CloudConfigurationModel cloudConfiguration;

  @Cascade(CascadeType.DELETE)
  @OneToMany(mappedBy = "cloudModel", orphanRemoval = true)
  private List<LocationModel> locations;

  @Cascade(CascadeType.DELETE)
  @OneToMany(mappedBy = "cloudModel", orphanRemoval = true)
  private List<ResourceModel> resources;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CloudType cloudType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CloudState cloudState;

  @Column(nullable = true)
  @Nullable private String diagnostic;


  /**
   * Empty constructor. Needed by hibernate.
   */
  protected CloudModel() {
  }

  public CloudModel(String cloudId, TenantModel tenantModel, ApiModel apiModel,
      @Nullable String endpoint,
      CloudCredentialModel cloudCredentialModel, CloudConfigurationModel cloudConfiguration,
      CloudType cloudType, CloudState cloudState, @Nullable String diagnostic) {
    this.cloudId = cloudId;
    this.tenantModel = tenantModel;
    this.apiModel = apiModel;
    this.endpoint = endpoint;
    this.cloudCredential = cloudCredentialModel;
    this.cloudConfiguration = cloudConfiguration;
    this.cloudType = cloudType;
    this.cloudState = cloudState;
    this.diagnostic = diagnostic;
  }

  public String getCloudId() {
    return cloudId;
  }

  public TenantModel getTenantModel() {
    return tenantModel;
  }

  public ApiModel getApiModel() {
    return apiModel;
  }

  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  public CloudCredentialModel getCloudCredential() {
    return cloudCredential;
  }

  public CloudConfigurationModel getCloudConfiguration() {
    return cloudConfiguration;
  }

  public CloudType getCloudType() {
    return cloudType;
  }

  public CloudModel setCloudType(CloudType cloudType) {
    this.cloudType = cloudType;
    return this;
  }

  public CloudState getCloudState() {
    return cloudState;
  }

  @Nullable
  public String getDiagnostic() {
    return diagnostic;
  }
}
