package io.github.cloudiator.persistance;

import javax.annotation.Nullable;

public interface ApiModelRepository extends ModelRepository<ApiModel> {

  @Nullable
  ApiModel findByProviderName(String providerName);

}
