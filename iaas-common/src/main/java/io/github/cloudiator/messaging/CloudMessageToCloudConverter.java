package io.github.cloudiator.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.ExtendedCloudBuilder;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Cloud.Builder;


/**
 * Created by daniel on 31.05.17.
 */
public class CloudMessageToCloudConverter implements
    TwoWayConverter<IaasEntities.Cloud, ExtendedCloud> {

  private static final ApiMessageToApi API_CONVERTER = ApiMessageToApi.INSTANCE;
  private static final ConfigurationMessageToConfiguration CONFIGURATION_CONVERTER = ConfigurationMessageToConfiguration.INSTANCE;
  private static final CredentialMessageToCredential CREDENTIAL_CONVERTER = CredentialMessageToCredential.INSTANCE;
  private static final CloudTypeMessageToCloudType CLOUD_TYPE_CONVERTER = CloudTypeMessageToCloudType.INSTANCE;

  public static CloudMessageToCloudConverter INSTANCE = new CloudMessageToCloudConverter();

  private CloudMessageToCloudConverter() {
  }

  public static class CloudStateConverter implements
      TwoWayConverter<IaasEntities.CloudState, CloudState> {

    public static CloudStateConverter INSTANCE = new CloudStateConverter();

    private CloudStateConverter() {
    }

    @Override
    public IaasEntities.CloudState applyBack(CloudState cloudState) {
      switch (cloudState) {
        case OK:
          return IaasEntities.CloudState.CLOUD_STATE_OK;
        case NEW:
          return IaasEntities.CloudState.CLOUD_STATE_NEW;
        case ERROR:
          return IaasEntities.CloudState.CLOUD_STATE_ERROR;
        case DELETED:
          return IaasEntities.CloudState.CLOUD_STATE_DELETED;
        default:
          throw new AssertionError("Unknown cloud state " + cloudState);
      }
    }

    @Override
    public CloudState apply(IaasEntities.CloudState cloudState) {

      switch (cloudState) {
        case CLOUD_STATE_DELETED:
          return CloudState.DELETED;
        case CLOUD_STATE_OK:
          return CloudState.OK;
        case CLOUD_STATE_ERROR:
          return CloudState.ERROR;
        case CLOUD_STATE_NEW:
          return CloudState.NEW;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown cloud state " + cloudState);

      }
    }
  }


  @Override
  public ExtendedCloud apply(IaasEntities.Cloud cloud) {
    ExtendedCloudBuilder cloudBuilder = ExtendedCloudBuilder.newBuilder()
        .credentials(CREDENTIAL_CONVERTER.apply(cloud.getCredential()))
        .api(API_CONVERTER.apply(cloud.getApi()))
        .configuration(CONFIGURATION_CONVERTER.apply(cloud.getConfiguration()))
        .cloudType(CLOUD_TYPE_CONVERTER.apply(cloud.getCloudType()))
        .state(CloudStateConverter.INSTANCE.apply(cloud.getState()));

    if (!Strings.isNullOrEmpty(cloud.getEndpoint())) {
      cloudBuilder.endpoint(cloud.getEndpoint());
    }

    if (!Strings.isNullOrEmpty(cloud.getDiagnostic())) {
      cloudBuilder.diagnostic(cloud.getDiagnostic());
    }

    return cloudBuilder.build();
  }

  @Override
  public IaasEntities.Cloud applyBack(ExtendedCloud cloud) {
    final Builder cloudBuilder = IaasEntities.Cloud.newBuilder()
        .setId(cloud.id())
        .setCredential(CREDENTIAL_CONVERTER.applyBack(cloud.credential()))
        .setApi(API_CONVERTER.applyBack(cloud.api()))
        .setConfiguration(CONFIGURATION_CONVERTER.applyBack(cloud.configuration()))
        .setCloudType(CLOUD_TYPE_CONVERTER.applyBack(cloud.cloudType()))
        .setState(CloudStateConverter.INSTANCE.applyBack(cloud.state()));

    if (cloud.endpoint().isPresent()) {
      cloudBuilder.setEndpoint(cloud.endpoint().get());
    }

    if (cloud.diagnostic().isPresent()) {
      cloudBuilder.setDiagnostic(cloud.diagnostic().get());
    }

    return cloudBuilder.build();

  }
}
