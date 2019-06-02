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

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
class ByonNodeModel extends Model {

  @Column(nullable = false, unique = true)
  private String domainId;

  @Column(nullable = false)
  private String name;

  @ManyToOne(optional = false)
  private TenantModel tenantModel;

  @Column(nullable = false)
  private boolean allocated;

  @OneToOne(optional = false, orphanRemoval = true)
  private NodePropertiesModel nodeProperties;

  @OneToOne(optional = true, orphanRemoval = true)
  @Nullable
  private LoginCredentialModel loginCredential;

  @OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
  @Nullable
  private IpGroupModel ipGroup;

  @Lob
  @Nullable
  private String nodeCandidate;

  @Nullable
  @Lob
  private String diagnostic;

  @Nullable
  @Lob
  private String reason;

  /**
   * Empty constructor for hibernate
   */
  protected ByonNodeModel() {

  }

  ByonNodeModel(String domainId, String name, TenantModel tenantModel,
      NodePropertiesModel nodeProperties,
      @Nullable LoginCredentialModel loginCredential,
      @Nullable IpGroupModel ipGroup, @Nullable String nodeCandidate,
      @Nullable String diagnostic, @Nullable String reason) {

    checkNotNull(domainId, "domainId is null");
    checkNotNull(name, "name is null");
    checkNotNull(tenantModel, "tenantModel is null");
    checkNotNull(nodeProperties, "nodeProperties is null");

    this.domainId = domainId;
    this.name = name;
    this.tenantModel = tenantModel;
    this.allocated = false;
    this.nodeProperties = nodeProperties;
    this.loginCredential = loginCredential;
    this.ipGroup = ipGroup;
    this.nodeCandidate = nodeCandidate;
    this.diagnostic = diagnostic;
    this.reason = reason;

  }

  public TenantModel getTenantModel() {
    return tenantModel;
  }

  public NodePropertiesModel getNodeProperties() {
    return nodeProperties;
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

  public String getDomainId() {
    return domainId;
  }

  public String getName() {
    return name;
  }

  public boolean getAllocated() {
    return allocated;
  }

  @Nullable
  public String getDiagnostic() {
    return diagnostic;
  }

  public ByonNodeModel setDiagnostic(@Nullable String diagnostic) {
    this.diagnostic = diagnostic;
    return this;
  }

  @Nullable
  public String getReason() {
    return reason;
  }

  public ByonNodeModel setReason(@Nullable String reason) {
    this.reason = reason;
    return this;
  }

  public String setName(String name) {
    this.name = name;
    return this.name;
  }

  public boolean setAllocated(boolean allocated) {
    this.allocated = allocated;
    return this.allocated;
  }

  public ByonNodeModel setNodeProperties(
      NodePropertiesModel nodeProperties) {
    this.nodeProperties = nodeProperties;
    return this;
  }

  public ByonNodeModel setLoginCredential(
      @Nullable LoginCredentialModel loginCredential) {
    this.loginCredential = loginCredential;
    return this;
  }

  public ByonNodeModel setIpGroup(@Nullable IpGroupModel ipGroup) {
    this.ipGroup = ipGroup;
    return this;
  }

  @Nullable
  public String getNodeCandidate() {
    return nodeCandidate;
  }

  public void setNodeCandidate(@Nullable String nodeCandidate) {
    this.nodeCandidate = nodeCandidate;
  }
}
