package io.github.cloudiator.persistance;

import java.util.List;

public interface NodeGroupModelRepository extends ModelRepository<NodeGroupModel> {

  NodeGroupModel findByTenantAndDomainId(String userId, String domainId);

  List<NodeGroupModel> findByTenant(String userId);

}
