package io.github.cloudiator.domain;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpAddressType;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class NodeImpl implements Node {

  private final NodeProperties nodeProperties;
  @Nullable
  private final LoginCredential loginCredential;
  private final NodeType nodeType;
  private final Set<IpAddress> ipAddresses;
  private final String id;

  NodeImpl(NodeProperties nodeProperties,
      @Nullable LoginCredential loginCredential, NodeType nodeType,
      Set<IpAddress> ipAddresses, String id) {
    this.nodeProperties = nodeProperties;
    this.loginCredential = loginCredential;
    this.nodeType = nodeType;
    this.ipAddresses = ipAddresses;
    this.id = id;
  }

  @Override
  public NodeProperties nodeProperties() {
    return nodeProperties;
  }

  @Override
  public Optional<LoginCredential> loginCredential() {
    return Optional.ofNullable(loginCredential);
  }

  @Override
  public NodeType type() {
    return nodeType;
  }

  @Override
  public Set<IpAddress> ipAddresses() {
    return ipAddresses;
  }

  public Set<IpAddress> publicIpAddresses() {
    return ipAddresses.stream().filter(ipAddress -> ipAddress.type().equals(IpAddressType.PUBLIC))
        .collect(Collectors
            .toSet());
  }

  public Set<IpAddress> privateIpAddresses() {
    return ipAddresses.stream().filter(ipAddress -> ipAddress.type().equals(IpAddressType.PRIVATE))
        .collect(Collectors
            .toSet());
  }

  @Override
  public IpAddress connectTo() {

    SortedSet<IpAddress> sortedSet = new TreeSet<>(new Comparator<IpAddress>() {
      @Override
      public int compare(IpAddress ipAddress, IpAddress t1) {
        if (ipAddress.type() == t1.type()) {
          return 0;
        }
        if (ipAddress.type().equals(IpAddressType.PUBLIC)) {
          return -1;
        }
        return 1;
      }
    });
    sortedSet.addAll(ipAddresses);

    for (IpAddress ipAddress : sortedSet) {
      if (ipAddress.isPingable()) {
        return ipAddress;
      }
    }

    return sortedSet.first();
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("properties", nodeProperties)
        .add("loginCredential", loginCredential).add("type", nodeType).add("ipAddresses",
            Joiner.on(",").join(ipAddresses)).toString();
  }
}
