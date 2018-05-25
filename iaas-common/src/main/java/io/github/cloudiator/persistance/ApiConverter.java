package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.ApiBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class ApiConverter implements OneWayConverter<ApiModel, Api> {

  @Nullable
  @Override
  public Api apply(@Nullable ApiModel apiModel) {
    if (apiModel == null) {
      return null;
    }
    return ApiBuilder.newBuilder().providerName(apiModel.getProviderName()).build();
  }
}
