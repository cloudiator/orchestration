package io.github.cloudiator.iaas.common.persistance.repositories;

import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import java.util.List;
import javax.annotation.Nullable;
import javax.tools.DocumentationTool.Location;

/**
 * Created by daniel on 01.06.17.
 */
public interface LocationModelRepository extends ModelRepository<LocationModel> {

  @Nullable
  LocationModel findByCloudUniqueId(String cloudUniqueId);

  List<LocationModel> findByTenant(String tenantId);

}
