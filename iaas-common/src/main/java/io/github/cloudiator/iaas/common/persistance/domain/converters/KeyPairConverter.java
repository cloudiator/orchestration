package io.github.cloudiator.iaas.common.persistance.domain.converters;

import de.uniulm.omi.cloudiator.sword.domain.KeyPair;
import de.uniulm.omi.cloudiator.sword.domain.KeyPairBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

/**
 * Created by daniel on 29.06.17.
 */
public class KeyPairConverter implements OneWayConverter<KeyPairCloudModel, KeyPair> {

  private final LocationConverter locationConverter = new LocationConverter();

  @Nullable
  @Override
  public KeyPair apply(@Nullable KeyPairCloudModel keyPairCloudModel) {
    if (keyPairCloudModel == null) {
      return null;
    }

    return KeyPairBuilder.newBuilder().id(keyPairCloudModel.getCloudUniqueId())
        .providerId(keyPairCloudModel.getProviderId())
        .location(locationConverter.apply(keyPairCloudModel.getLocationModel()))
        .name(keyPairCloudModel.getName()).build();
  }
}
