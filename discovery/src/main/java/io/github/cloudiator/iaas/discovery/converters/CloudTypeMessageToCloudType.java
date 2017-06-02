package io.github.cloudiator.iaas.discovery.converters;

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
        return IaasEntities.CloudType.PUBLIC;
      case PRIVATE:
        return IaasEntities.CloudType.PRIVATE;
      default:
        throw new AssertionError(String.format("Unrecognized cloudType %s.", cloudType));
    }
  }

  @Override
  public CloudType apply(IaasEntities.CloudType cloudType) {
    switch (cloudType) {
      case PRIVATE:
        return CloudType.PRIVATE;
      case PUBLIC:
        return CloudType.PUBLIC;
      case UNRECOGNIZED:
      default:
        throw new AssertionError(String.format("Unrecognized cloudType %s", cloudType));
    }
  }
}
