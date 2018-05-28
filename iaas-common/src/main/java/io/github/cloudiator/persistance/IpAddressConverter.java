package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.IpAddresses;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class IpAddressConverter implements OneWayConverter<IpAddressModel, IpAddress> {

  @Nullable
  @Override
  public IpAddress apply(@Nullable IpAddressModel ipAddressModel) {
    if (ipAddressModel == null) {
      return null;
    }

    return IpAddresses
        .of(ipAddressModel.getIp(), ipAddressModel.getType(), ipAddressModel.getVersion());
  }
}
