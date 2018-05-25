package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.ApiBuilder;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudBuilder;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class CloudConverter implements OneWayConverter<CloudModel, Cloud> {

  private final static CloudCredentialConverter CLOUD_CREDENTIAL_CONVERTER = new CloudCredentialConverter();
  private final static CloudConfigurationConverter CLOUD_CONFIGURATION_CONVERTER = new CloudConfigurationConverter();

  @Nullable
  @Override
  public Cloud apply(@Nullable CloudModel cloudModel) {

    if (cloudModel == null) {
      return null;
    }

    final Api api = ApiBuilder.newBuilder().providerName(cloudModel.getApiModel().getProviderName())
        .build();
    final CloudCredential cloudCredential = CLOUD_CREDENTIAL_CONVERTER
        .apply(cloudModel.getCloudCredential());

    final Configuration configuration = CLOUD_CONFIGURATION_CONVERTER
        .apply(cloudModel.getCloudConfiguration());

    return CloudBuilder.newBuilder().api(api).cloudType(cloudModel.getCloudType())
        .configuration(configuration).credentials(cloudCredential)
        .endpoint(cloudModel.getEndpoint()).build();
  }
}
