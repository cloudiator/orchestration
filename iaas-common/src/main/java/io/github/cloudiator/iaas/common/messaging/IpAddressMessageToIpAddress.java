package io.github.cloudiator.iaas.common.messaging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.cloudiator.messages.entities.IaasEntities.IpAddress;
import org.cloudiator.messages.entities.IaasEntities.IpAddressType;
import org.cloudiator.messages.entities.IaasEntities.IpVersion;

public class IpAddressMessageToIpAddress {

  public IpAddress applyBack(String s, IpAddressType ipAddressType) {
    return IpAddress.newBuilder().setIp(s).setVersion(getIpVersion(s)).setType(ipAddressType)
        .build();
  }

  public String apply(IpAddress ipAddress) {
    return ipAddress.getIp();
  }

  private IpVersion getIpVersion(String ip) {
    try {
      InetAddress address = java.net.InetAddress.getByName(ip);
      if (address instanceof java.net.Inet4Address) {
        return IpVersion.V4;
      }
      if (address instanceof java.net.Inet6Address) {
        return IpVersion.v6;
      }
      throw new IllegalArgumentException(ip);
    } catch (UnknownHostException uhe) {
      throw new IllegalArgumentException(ip, uhe);
    }
  }
}
