package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
class NodeGroupModel extends Model {

  @Column(nullable = false)
  private String domainId;

  @ManyToOne
  private TenantModel tenantModel;

  @OneToMany(mappedBy = "nodeGroupModel")
  private List<NodeModel> nodes;

  protected NodeGroupModel() {

  }

  NodeGroupModel(String id, TenantModel tenantModel) {
    checkNotNull(id, "id is null");
    checkNotNull(tenantModel, "tenantModel is null");
    this.domainId = id;
    this.tenantModel = tenantModel;
  }

  public String getDomainId() {
    return domainId;
  }

  public List<NodeModel> getNodes() {
    return ImmutableList.copyOf(nodes);
  }

  public NodeGroupModel addNode(NodeModel node) {
    if (nodes == null) {
      nodes = new ArrayList<>();
    }
    checkArgument(tenantModel.getUserId().equals(node.getTenantModel().getUserId()),
        String.format("Node %s belongs to a different tenant as this node group %s.", node, this));

    nodes.add(node);
    return this;
  }
}
