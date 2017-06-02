package io.github.cloudiator.iaas.common.persistance.repositories;

import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import javax.annotation.Nullable;

/**
 * Created by daniel on 01.06.17.
 */
public interface LocationModelRepository extends ModelRepository<LocationModel>{

  @Nullable
  LocationModel findByCloudUniqueId(String cloudUniqueId);

}
