package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.sword.domain.CloudType;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudTypeMessageToCloudType implements
    TwoWayConverter<IaasEntities.CloudType, CloudType> {

  @Override
  public IaasEntities.CloudType applyBack(CloudType cloudType) {
    switch (cloudType) {
      case PUBLIC:
        return IaasEntities.CloudType.PUBLIC_CLOUD;
      case PRIVATE:
        return IaasEntities.CloudType.PRIVATE_CLOUD;
      default:
        throw new AssertionError(String.format("Unrecognized cloudType %s.", cloudType));
    }
  }

  @Override
  public CloudType apply(IaasEntities.CloudType cloudType) {
    switch (cloudType) {
      case PRIVATE_CLOUD:
        return CloudType.PRIVATE;
      case PUBLIC_CLOUD:
        return CloudType.PUBLIC;
      case UNRECOGNIZED:
      default:
        throw new AssertionError(String.format("Unrecognized cloudType %s", cloudType));
    }
  }
}
