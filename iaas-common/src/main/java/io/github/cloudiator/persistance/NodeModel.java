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

import io.github.cloudiator.domain.NodeType;
import java.util.Collections;
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

  @Column(nullable = false)
  private String name;

  @ManyToOne(optional = false)
  private TenantModel tenantModel;

  @OneToOne(optional = false)
  private NodePropertiesModel nodeProperties;

  @OneToOne(optional = true)
  @Nullable
  private LoginCredentialModel loginCredential;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NodeType type;

  @OneToOne(optional = true, cascade = CascadeType.ALL)
  @Nullable
  private IpGroupModel ipGroup;

  @ManyToOne
  @Nullable
  private NodeGroupModel nodeGroupModel;

  /**
   * Empty constructor for hibernate
   */
  protected NodeModel() {

  }

  NodeModel(String domainId, String name, TenantModel tenantModel,
      NodePropertiesModel nodeProperties,
      @Nullable LoginCredentialModel loginCredential, NodeType nodeType,
      @Nullable IpGroupModel ipGroup, @Nullable NodeGroupModel nodeGroupModel) {

    checkNotNull(domainId, "domainId is null");
    checkNotNull(name, "name is null");
    checkNotNull(tenantModel, "tenantModel is null");
    checkNotNull(nodeProperties, "nodeProperties is null");
    checkNotNull(nodeType, "nodeType is null");

    this.domainId = domainId;
    this.name = name;
    this.tenantModel = tenantModel;
    this.nodeProperties = nodeProperties;
    this.loginCredential = loginCredential;
    this.type = nodeType;
    this.ipGroup = ipGroup;
    this.nodeGroupModel = nodeGroupModel;

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
}
