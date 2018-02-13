package io.github.cloudiator.persistance;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Created by daniel on 31.05.17.
 */
public interface CloudModelRepository extends ModelRepository<CloudModel> {

  @Nullable
  CloudModel getByCloudId(String cloudId);

  List<CloudModel> getByTenant(String userId);

  @Nullable
  CloudModel getByTenantAndId(String userId, String cloudId);


}
