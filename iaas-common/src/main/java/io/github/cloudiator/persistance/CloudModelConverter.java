package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.ApiBuilder;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.ExtendedCloudBuilder;
import javax.annotation.Nullable;

class CloudModelConverter implements OneWayConverter<CloudModel, ExtendedCloud> {

  private final static CloudCredentialConverter CLOUD_CREDENTIAL_CONVERTER = new CloudCredentialConverter();
  private final static CloudConfigurationConverter CLOUD_CONFIGURATION_CONVERTER = new CloudConfigurationConverter();

  @Nullable
  @Override
  public ExtendedCloud apply(@Nullable CloudModel cloudModel) {

    if (cloudModel == null) {
      return null;
    }

    final Api api = ApiBuilder.newBuilder().providerName(cloudModel.getApiModel().getProviderName())
        .build();
    final CloudCredential cloudCredential = CLOUD_CREDENTIAL_CONVERTER
        .apply(cloudModel.getCloudCredential());

    final Configuration configuration = CLOUD_CONFIGURATION_CONVERTER
        .apply(cloudModel.getCloudConfiguration());

    return ExtendedCloudBuilder.newBuilder().api(api).cloudType(cloudModel.getCloudType())
        .configuration(configuration).credentials(cloudCredential)
        .endpoint(cloudModel.getEndpoint()).state(cloudModel.getCloudState())
        .diagnostic(cloudModel.getDiagnostic()).userId(cloudModel.getTenantModel().getUserId())
        .build();
  }
}
