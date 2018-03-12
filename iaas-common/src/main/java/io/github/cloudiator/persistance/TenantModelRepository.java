package io.github.cloudiator.persistance;

/**
 * Created by daniel on 31.05.17.
 */
public interface TenantModelRepository extends ModelRepository<TenantModel> {

  TenantModel findByUserId(String userId);

  TenantModel createOrGet(String userId);

}
