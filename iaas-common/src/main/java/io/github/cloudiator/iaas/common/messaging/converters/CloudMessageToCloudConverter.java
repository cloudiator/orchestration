package io.github.cloudiator.iaas.common.messaging.converters;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Cloud.Builder;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudMessageToCloudConverter implements TwoWayConverter<IaasEntities.Cloud, Cloud> {

  private ApiMessageToApi apiConverter = new ApiMessageToApi();
  private ConfigurationMessageToConfiguration configurationConverter =
      new ConfigurationMessageToConfiguration();
  private CredentialMessageToCredential credentialConverter = new CredentialMessageToCredential();
  private CloudTypeMessageToCloudType cloudTypeConverter = new CloudTypeMessageToCloudType();

  @Override
  public Cloud apply(IaasEntities.Cloud cloud) {
    return CloudBuilder.newBuilder()
        .credentials(credentialConverter.apply(cloud.getCredential()))
        .api(apiConverter.apply(cloud.getApi()))
        .configuration(configurationConverter.apply(cloud.getConfiguration()))
        .endpoint(cloud.getEndpoint())
        .cloudType(cloudTypeConverter.apply(cloud.getCloudType()))
        .build();
  }

  @Override
  public IaasEntities.Cloud applyBack(Cloud cloud) {
    final Builder cloudBuilder = IaasEntities.Cloud.newBuilder()
        .setId(cloud.id())
        .setCredential(credentialConverter.applyBack(cloud.credential()))
        .setApi(apiConverter.applyBack(cloud.api()))
        .setConfiguration(configurationConverter.applyBack(cloud.configuration()))
        .setCloudType(cloudTypeConverter.applyBack(cloud.cloudType()));

    if (cloud.endpoint().isPresent()) {
      cloudBuilder.setEndpoint(cloud.endpoint().get());
    }

    return cloudBuilder.build();

  }
}
