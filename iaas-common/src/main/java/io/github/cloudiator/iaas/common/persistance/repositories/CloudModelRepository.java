package io.github.cloudiator.iaas.common.persistance.repositories;

import de.uniulm.omi.cloudiator.persistance.repositories.ModelRepository;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import java.util.List;

/**
 * Created by daniel on 31.05.17.
 */
public interface CloudModelRepository extends ModelRepository<CloudModel> {

  CloudModel getByCloudId(String cloudId);

  List<CloudModel> getByTenant(String userId);


}
