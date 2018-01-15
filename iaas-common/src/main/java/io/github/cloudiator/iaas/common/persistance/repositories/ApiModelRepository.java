package io.github.cloudiator.iaas.common.persistance.repositories;

import de.uniulm.omi.cloudiator.persistance.repositories.ModelRepository;
import io.github.cloudiator.iaas.common.persistance.entities.ApiModel;
import javax.annotation.Nullable;

public interface ApiModelRepository extends ModelRepository<ApiModel> {

  @Nullable
  ApiModel findByProviderName(String providerName);

}
