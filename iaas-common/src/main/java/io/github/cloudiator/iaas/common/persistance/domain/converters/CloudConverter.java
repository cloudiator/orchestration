package io.github.cloudiator.iaas.common.persistance.domain.converters;

import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.ApiBuilder;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudBuilder;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.sword.domain.ConfigurationBuilder;
import de.uniulm.omi.cloudiator.sword.domain.CredentialsBuilder;
import de.uniulm.omi.cloudiator.sword.domain.Properties;
import de.uniulm.omi.cloudiator.sword.domain.PropertiesBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import javax.annotation.Nullable;

public class CloudConverter implements OneWayConverter<CloudModel, Cloud> {

  @Nullable
  @Override
  public Cloud apply(@Nullable CloudModel cloudModel) {

    if (cloudModel == null) {
      return null;
    }

    final Api api = ApiBuilder.newBuilder().providerName(cloudModel.getApiModel().getProviderName())
        .build();
    final CloudCredential cloudCredential = CredentialsBuilder.newBuilder()
        .user(cloudModel.getCloudCredential().getUser())
        .password(cloudModel.getCloudCredential().getPassword()).build();
    final PropertiesBuilder propertiesBuilder = PropertiesBuilder.newBuilder();
    cloudModel.getCloudConfiguration().getProperties().stream().forEach(
        propertyModel -> propertiesBuilder
            .putProperty(propertyModel.getKey(), propertyModel.getValue()));
    Properties properties = propertiesBuilder.build();
    final Configuration configuration = ConfigurationBuilder.newBuilder()
        .nodeGroup(cloudModel.getCloudConfiguration().getNodeGroup()).properties(properties)
        .build();

    return CloudBuilder.newBuilder().api(api).cloudType(cloudModel.getCloudType())
        .configuration(configuration).credentials(cloudCredential)
        .endpoint(cloudModel.getEndpoint()).build();
  }
}
