package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import org.cloudiator.messages.entities.IaasEntities.NewCloud;

/**
 * Created by daniel on 15.03.17.
 */
public class NewCloudMessageToCloud
    implements OneWayConverter<NewCloud, Cloud> {

  private ApiMessageToApi apiConverter = new ApiMessageToApi();
  private ConfigurationMessageToConfiguration configurationConverter =
      new ConfigurationMessageToConfiguration();
  private CredentialMessageToCredential credentialConverter = new CredentialMessageToCredential();
  private CloudTypeMessageToCloudType cloudTypeConverter = new CloudTypeMessageToCloudType();

  @Override
  public Cloud apply(NewCloud cloud) {
    return CloudBuilder.newBuilder()
        .credentials(credentialConverter.apply(cloud.getCredential()))
        .api(apiConverter.apply(cloud.getApi()))
        .configuration(configurationConverter.apply(cloud.getConfiguration()))
        .endpoint(cloud.getEndpoint())
        .cloudType(cloudTypeConverter.apply(cloud.getCloudType()))
        .build();
  }


}
