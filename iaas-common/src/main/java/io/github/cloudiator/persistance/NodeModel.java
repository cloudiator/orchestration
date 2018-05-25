package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cloudiator.domain.NodeType;
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

  /**
   * Empty constructor for hibernate
   */
  protected NodeModel() {

  }

  NodeModel(TenantModel tenantModel, NodePropertiesModel nodeProperties,
      @Nullable LoginCredentialModel loginCredential, NodeType nodeType,
      @Nullable IpGroupModel ipGroup) {

    checkNotNull(tenantModel, "tenantModel is null");
    checkNotNull(nodeProperties, "nodeProperties is null");
    checkNotNull(nodeType, "nodeType is null");

    this.tenantModel = tenantModel;
    this.nodeProperties = nodeProperties;
    this.loginCredential = loginCredential;
    this.type = nodeType;
    this.ipGroup = ipGroup;

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
}
