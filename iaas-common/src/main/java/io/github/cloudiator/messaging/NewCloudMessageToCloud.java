package io.github.cloudiator.messaging;

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
  public Cloud apply(NewCloud newCloud) {
    final CloudBuilder cloudBuilder = CloudBuilder.newBuilder()
        .credentials(credentialConverter.apply(newCloud.getCredential()))
        .api(apiConverter.apply(newCloud.getApi()))
        .configuration(configurationConverter.apply(newCloud.getConfiguration()))
        .cloudType(cloudTypeConverter.apply(newCloud.getCloudType()));

    if (newCloud.getEndpoint() == null || newCloud.getEndpoint().equals("")) {
      cloudBuilder.endpoint(null);
    } else {
      cloudBuilder.endpoint(newCloud.getEndpoint());
    }

    return cloudBuilder.build();
  }


}
