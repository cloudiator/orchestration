/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpAddressType;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpVersion;
import de.uniulm.omi.cloudiator.sword.domain.IpAddresses;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;

public class IpAddressMessageToIpAddress implements
    TwoWayConverter<IaasEntities.IpAddress, IpAddress> {

  private final IpAddressTypeMessageToIpAddressType typeConverter = new IpAddressTypeMessageToIpAddressType();
  private final IpAddressVersionMessageToIpAddressVersion versionConverter = new IpAddressVersionMessageToIpAddressVersion();

  @Override
  public IaasEntities.IpAddress applyBack(IpAddress ipAddress) {
    return IaasEntities.IpAddress.newBuilder()
        .setIp(ipAddress.ip())
        .setVersion(versionConverter.applyBack(ipAddress.version()))
        .setType(typeConverter.applyBack(ipAddress.type()))
        .build();
  }

  @Override
  public IpAddress apply(IaasEntities.IpAddress ipAddress) {
    return IpAddresses.of(ipAddress.getIp(), typeConverter.apply(ipAddress.getType()),
        versionConverter.apply(ipAddress.getVersion()));
  }

  private static class IpAddressTypeMessageToIpAddressType implements
      TwoWayConverter<IaasEntities.IpAddressType, IpAddressType> {

    @Override
    public IaasEntities.IpAddressType applyBack(IpAddressType ipAddressType) {
      switch (ipAddressType) {
        case PUBLIC:
          return IaasEntities.IpAddressType.PUBLIC_IP;
        case PRIVATE:
          return IaasEntities.IpAddressType.PRIVATE_IP;
        default:
          throw new AssertionError(String.format("IpAddressType %s is not known.", ipAddressType));
      }
    }

    @Override
    public IpAddressType apply(IaasEntities.IpAddressType ipAddressType) {
      switch (ipAddressType) {
        case PRIVATE_IP:
          return IpAddressType.PRIVATE;
        case PUBLIC_IP:
          return IpAddressType.PUBLIC;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(String.format("IpAddressType %s is not known.", ipAddressType));
      }
    }
  }

  private static class IpAddressVersionMessageToIpAddressVersion implements
      TwoWayConverter<IaasEntities.IpVersion, IpVersion> {

    @Override
    public IaasEntities.IpVersion applyBack(IpVersion ipVersion) {
      switch (ipVersion) {
        case V4:
          return IaasEntities.IpVersion.V4;
        case V6:
          return IaasEntities.IpVersion.V6;
        default:
          throw new AssertionError(String.format("IpVersion %s is not known.", ipVersion));
      }
    }

    @Override
    public IpVersion apply(IaasEntities.IpVersion ipVersion) {
      switch (ipVersion) {
        case V4:
          return IpVersion.V4;
        case V6:
          return IpVersion.V6;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(String.format("IpVersion %s is not known.", ipVersion));
      }
    }
  }

}
