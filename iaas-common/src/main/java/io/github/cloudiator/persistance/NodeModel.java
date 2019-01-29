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
import static com.google.common.base.Preconditions.checkState;

import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.domain.NodeType;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
class NodeModel extends Model {

  @Column(nullable = false, unique = true)
  private String domainId;

  @Nullable
  private String originId;

  @Column(nullable = false)
  private String name;

  @ManyToOne(optional = false)
  private TenantModel tenantModel;

  public NodeModel setNodeProperties(
      NodePropertiesModel nodeProperties) {
    this.nodeProperties = nodeProperties;
    return this;
  }

  public NodeModel setLoginCredential(
      @Nullable LoginCredentialModel loginCredential) {
    this.loginCredential = loginCredential;
    return this;
  }

  public NodeModel setIpGroup(@Nullable IpGroupModel ipGroup) {
    this.ipGroup = ipGroup;
    return this;
  }

  @OneToOne(optional = false, orphanRemoval = true)
  private NodePropertiesModel nodeProperties;

  @OneToOne(optional = true, orphanRemoval = true)
  @Nullable
  private LoginCredentialModel loginCredential;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NodeType type;

  @OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
  @Nullable
  private IpGroupModel ipGroup;

  @Nullable
  private NodeGroupModel nodeGroupModel;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NodeState nodeState;

  @Nullable
  private String diagnostic;

  @Nullable
  private String reason;

  /**
   * Empty constructor for hibernate
   */
  protected NodeModel() {

  }

  NodeModel(String domainId, @Nullable String originId, String name, TenantModel tenantModel,
      NodePropertiesModel nodeProperties,
      @Nullable LoginCredentialModel loginCredential, NodeType nodeType,
      @Nullable IpGroupModel ipGroup, @Nullable NodeGroupModel nodeGroupModel,
      NodeState nodeState, @Nullable String diagnostic, @Nullable String reason) {

    checkNotNull(domainId, "domainId is null");
    checkNotNull(name, "name is null");
    checkNotNull(tenantModel, "tenantModel is null");
    checkNotNull(nodeProperties, "nodeProperties is null");
    checkNotNull(nodeType, "nodeType is null");
    checkNotNull(nodeState, "nodeState is null");

    this.domainId = domainId;
    this.originId = originId;
    this.name = name;
    this.tenantModel = tenantModel;
    this.nodeProperties = nodeProperties;
    this.loginCredential = loginCredential;
    this.type = nodeType;
    this.ipGroup = ipGroup;
    this.nodeGroupModel = nodeGroupModel;
    this.nodeState = nodeState;
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

  public NodeType getType() {
    return type;
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

  public NodeModel assignGroup(NodeGroupModel nodeGroupModel) {
    checkState(this.nodeGroupModel == null, "Node Group was already assigned.");
    this.nodeGroupModel = nodeGroupModel;
    return this;
  }

  public String getName() {
    return name;
  }

  public NodeState getNodeState() {
    return nodeState;
  }

  public NodeModel setNodeState(NodeState nodeState) {
    this.nodeState = nodeState;
    return this;
  }

  @Nullable
  public String getDiagnostic() {
    return diagnostic;
  }

  public NodeModel setDiagnostic(@Nullable String diagnostic) {
    this.diagnostic = diagnostic;
    return this;
  }

  @Nullable
  public String getReason() {
    return reason;
  }

  public NodeModel setReason(@Nullable String reason) {
    this.reason = reason;
    return this;
  }

  @Nullable
  public String getOriginId() {
    return originId;
  }

  public NodeModel setOriginId(String originId) {

    if (Objects.equals(this.originId, originId)) {
      return this;
    }

    checkState(this.originId == null, String.format(
        "Changing the value of origin ID is not allowed. Was set to value %s, tried updating to %s",
        this.originId, originId));

    this.originId = originId;
    return this;
  }

  public NodeModel setType(NodeType type) {

    if (Objects.equals(this.type, type)) {
      return this;
    }

    checkState(NodeType.UNKOWN.equals(this.type), String.format(
        "Changing the value of type is only allowed if it was UNKNOWN. Was set to value %s, tried updating to %s",
        this.type, type));

    this.type = type;
    return this;
  }

  public String setName(String name) {
    this.name = name;
    return this.name;
  }
}
