package io.github.cloudiator.domain;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class NodeImpl implements Node {

  private final NodeProperties nodeProperties;
  @Nullable
  private final LoginCredential loginCredential;
  private final NodeType nodeType;
  private final Set<IpAddress> ipAddresses;
  private final String id;
  private final String name;

  NodeImpl(NodeProperties nodeProperties,
      @Nullable LoginCredential loginCredential, NodeType nodeType,
      Set<IpAddress> ipAddresses, String id, String name) {
    this.nodeProperties = nodeProperties;
    this.loginCredential = loginCredential;
    this.nodeType = nodeType;
    this.ipAddresses = ipAddresses;
    this.id = id;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
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

  @Nullable
  private static IpAddress returnFirstPingable(Set<IpAddress> addresses) {
    for (IpAddress ipAddress : addresses) {
      if (ipAddress.isPingable()) {
        return ipAddress;
      }
    }
    return null;
  }

  @Nullable
  private static IpAddress findConnectible(final Set<IpAddress> addresses) {
    if (addresses.size() == 1) {
      //noinspection ConstantConditions
      return addresses.stream().findFirst().get();
    } else if (addresses.size() > 1) {
      final IpAddress firstPingable = returnFirstPingable(addresses);
      if (firstPingable != null) {
        return firstPingable;
      } else {
        //noinspection ConstantConditions
        return addresses.stream().findFirst().get();
      }
    }
    return null;
  }

  @Override
  public IpAddress connectTo() {

    if (publicIpAddresses().isEmpty() && privateIpAddresses().isEmpty()) {
      throw new IllegalStateException(
          String.format("Node %s has no ip addresses. Can not connect.", this));
    }

    final IpAddress publicConnect = findConnectible(publicIpAddresses());
    if (publicConnect != null) {
      return publicConnect;
    }
    final IpAddress privateConnect = findConnectible(privateIpAddresses());
    if (privateConnect != null) {
      return privateConnect;
    }

    throw new IllegalStateException(
        String.format("Could not derive IpAddress to connect to for node %s", this));
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
