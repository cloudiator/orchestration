package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpAddressType;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface Node extends Identifiable {

  NodeProperties nodeProperties();

  Optional<LoginCredential> loginCredential();

  NodeType type();

  Set<IpAddress> ipAddresses();

  default Set<IpAddress> privateIpAddresses() {
    return ipAddresses().stream().filter(
        ipAddress -> IpAddressType.PRIVATE.equals(ipAddress.type())).collect(Collectors.toSet());
  }

  default Set<IpAddress> publicIpAddresses() {
    return ipAddresses().stream().filter(ipAddress -> IpAddressType.PUBLIC.equals(ipAddress.type()))
        .collect(Collectors.toSet());
  }

  IpAddress connectTo();
}
