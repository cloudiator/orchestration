package io.github.cloudiator.persistance;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
class NodeGroupModel extends Model {

  @OneToMany
  private List<NodeModel> nodes;

}
