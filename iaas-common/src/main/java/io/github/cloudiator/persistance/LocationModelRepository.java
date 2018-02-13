package io.github.cloudiator.persistance;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Created by daniel on 01.06.17.
 */
public interface LocationModelRepository extends ModelRepository<LocationModel> {

  @Nullable
  LocationModel findByCloudUniqueId(String cloudUniqueId);

  List<LocationModel> findByTenant(String tenantId);

  LocationModel findByCloudUniqueIdAndTenant(String userId, String locationId);

  List<LocationModel> findByTenantAndCloud(String tenantId, String cloudId);
}
