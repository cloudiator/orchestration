package io.github.cloudiator.iaas.common.domain;

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

  @Override
  public String id() {
    return id;
  }
}
