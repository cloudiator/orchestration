package io.github.cloudiator.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Cloud.Builder;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudMessageToCloudConverter implements TwoWayConverter<IaasEntities.Cloud, Cloud> {

  private static final ApiMessageToApi API_CONVERTER = ApiMessageToApi.INSTANCE;
  private static final ConfigurationMessageToConfiguration CONFIGURATION_CONVERTER = ConfigurationMessageToConfiguration.INSTANCE;
  private static final CredentialMessageToCredential CREDENTIAL_CONVERTER = CredentialMessageToCredential.INSTANCE;
  private static final CloudTypeMessageToCloudType CLOUD_TYPE_CONVERTER = CloudTypeMessageToCloudType.INSTANCE;

  public static CloudMessageToCloudConverter INSTANCE = new CloudMessageToCloudConverter();

  private CloudMessageToCloudConverter() {
  }


  @Override
  public Cloud apply(IaasEntities.Cloud cloud) {
    CloudBuilder cloudBuilder = CloudBuilder.newBuilder()
        .credentials(CREDENTIAL_CONVERTER.apply(cloud.getCredential()))
        .api(API_CONVERTER.apply(cloud.getApi()))
        .configuration(CONFIGURATION_CONVERTER.apply(cloud.getConfiguration()))
        .cloudType(CLOUD_TYPE_CONVERTER.apply(cloud.getCloudType()));

    if (!Strings.isNullOrEmpty(cloud.getEndpoint())) {
      cloudBuilder.endpoint(cloud.getEndpoint());
    }

    return cloudBuilder.build();
  }

  @Override
  public IaasEntities.Cloud applyBack(Cloud cloud) {
    final Builder cloudBuilder = IaasEntities.Cloud.newBuilder()
        .setId(cloud.id())
        .setCredential(CREDENTIAL_CONVERTER.applyBack(cloud.credential()))
        .setApi(API_CONVERTER.applyBack(cloud.api()))
        .setConfiguration(CONFIGURATION_CONVERTER.applyBack(cloud.configuration()))
        .setCloudType(CLOUD_TYPE_CONVERTER.applyBack(cloud.cloudType()));

    if (cloud.endpoint().isPresent()) {
      cloudBuilder.setEndpoint(cloud.endpoint().get());
    }

    return cloudBuilder.build();

  }
}
