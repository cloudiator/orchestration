package io.github.cloudiator.iaas.common.persistance.repositories;

import io.github.cloudiator.iaas.common.persistance.entities.Cloud;

/**
 * Created by daniel on 31.05.17.
 */
public interface CloudModelRepository extends ModelRepository<Cloud> {

  Cloud getByCloudId(String cloudId);



}
