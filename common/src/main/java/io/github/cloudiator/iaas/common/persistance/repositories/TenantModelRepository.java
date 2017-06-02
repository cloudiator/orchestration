package io.github.cloudiator.iaas.common.persistance.repositories;

import io.github.cloudiator.iaas.common.persistance.entities.Tenant;

/**
 * Created by daniel on 31.05.17.
 */
public interface TenantModelRepository extends ModelRepository<Tenant> {

  Tenant findByUserId(String userId);

  Tenant createOrGet(String userId);

}
