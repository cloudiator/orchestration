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

import de.uniulm.omi.cloudiator.persistance.entities.Model;
import de.uniulm.omi.cloudiator.sword.domain.CloudType;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Entity
public class CloudModel extends Model {

  @Column(unique = true, nullable = false, updatable = false)
  private String cloudId;
  @ManyToOne(optional = false)
  private Tenant tenant;

  @ManyToOne(optional = false)
  private ApiModel apiModel;

  @Nullable
  private String endpoint;

  @ManyToOne(optional = false)
  private CloudCredentialModel cloudCredential;

  @ManyToOne(optional = false)
  private CloudConfigurationModel cloudConfiguration;

  @Enumerated
  @Column(nullable = false)
  private CloudType cloudType;


  /**
   * Empty constructor. Needed by hibernate.
   */
  protected CloudModel() {
  }

  public CloudModel(String cloudId, Tenant tenant, ApiModel apiModel, @Nullable String endpoint,
      CloudCredentialModel cloudCredentialModel, CloudConfigurationModel cloudConfiguration,
      CloudType cloudType) {
    this.cloudId = cloudId;
    this.tenant = tenant;
    this.apiModel = apiModel;
    this.endpoint = endpoint;
    this.cloudCredential = cloudCredentialModel;
    this.cloudConfiguration = cloudConfiguration;
    this.cloudType = cloudType;
  }

  public String getCloudId() {
    return cloudId;
  }

  public Tenant getTenant() {
    return tenant;
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
}
