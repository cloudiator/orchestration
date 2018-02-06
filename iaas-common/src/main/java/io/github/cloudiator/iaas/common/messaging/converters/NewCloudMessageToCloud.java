package io.github.cloudiator.iaas.common.messaging.converters;

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
    final CloudBuilder cloudBuilder = CloudBuilder.newBuilder()
        .credentials(credentialConverter.apply(cloud.getCredential()))
        .api(apiConverter.apply(cloud.getApi()))
        .configuration(configurationConverter.apply(cloud.getConfiguration()))
        .cloudType(cloudTypeConverter.apply(cloud.getCloudType()));

    if (cloud.getEndpoint().equals("")) {
      cloudBuilder.endpoint(null);
    } else {
      cloudBuilder.endpoint(cloud.getEndpoint());
    }

    return cloudBuilder.build();
  }


}
