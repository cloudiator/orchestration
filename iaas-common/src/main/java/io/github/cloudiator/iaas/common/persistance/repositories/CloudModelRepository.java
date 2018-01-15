package io.github.cloudiator.iaas.common.persistance.repositories;

import de.uniulm.omi.cloudiator.persistance.repositories.ModelRepository;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
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
