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

  public static final NewCloudMessageToCloud INSTANCE = new NewCloudMessageToCloud();

  private static final ApiMessageToApi API_CONVERTER = ApiMessageToApi.INSTANCE;
  private static final ConfigurationMessageToConfiguration CONFIGURATION_CONVERTER =
      ConfigurationMessageToConfiguration.INSTANCE;
  private static final  CredentialMessageToCredential CREDENTIAL_CONVERTER = CredentialMessageToCredential.INSTANCE;
  private static final CloudTypeMessageToCloudType CLOUD_TYPE_CONVERTER = CloudTypeMessageToCloudType.INSTANCE;

  private NewCloudMessageToCloud() {

  }

  @Override
  public Cloud apply(NewCloud newCloud) {
    final CloudBuilder cloudBuilder = CloudBuilder.newBuilder()
        .credentials(CREDENTIAL_CONVERTER.apply(newCloud.getCredential()))
        .api(API_CONVERTER.apply(newCloud.getApi()))
        .configuration(CONFIGURATION_CONVERTER.apply(newCloud.getConfiguration()))
        .cloudType(CLOUD_TYPE_CONVERTER.apply(newCloud.getCloudType()));

    if (newCloud.getEndpoint() == null || newCloud.getEndpoint().equals("")) {
      cloudBuilder.endpoint(null);
    } else {
      cloudBuilder.endpoint(newCloud.getEndpoint());
    }

    return cloudBuilder.build();
  }


}
