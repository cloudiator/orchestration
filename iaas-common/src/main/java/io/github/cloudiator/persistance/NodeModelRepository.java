package io.github.cloudiator.persistance;

import java.util.List;
import javax.annotation.Nullable;

interface NodeModelRepository extends ModelRepository<NodeModel> {

  List<NodeModel> getByTenant(String userId);

  @Nullable
  NodeModel getByTenantAndId(String userId, String cloudId);

}
