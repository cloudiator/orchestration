package io.github.cloudiator.persistance;

import java.util.List;
import javax.annotation.Nullable;

interface VirtualMachineModelRepository extends ModelRepository<VirtualMachineModel> {

  @Nullable
  VirtualMachineModel findByCloudUniqueId(String cloudUniqueId);

  List<VirtualMachineModel> findByTenant(String tenant);

  VirtualMachineModel findByCloudUniqueIdAndTenant(String tenant, String cloudUniqueId);

  List<VirtualMachineModel> findByTenantAndCloud(String tenant, String cloudId);

}
