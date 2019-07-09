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

package io.github.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.remote.CompositeRemoteConnectionStrategy;
import io.github.cloudiator.remote.RemoteConnectionStrategy;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public abstract class AbstractNodeImpl implements BaseNode {

  private final static RemoteConnectionStrategy REMOTE_CONNECTION_STRATEGY = new CompositeRemoteConnectionStrategy();

  private final String id;
  private final String userId;
  private final NodeProperties nodeProperties;
  @Nullable
  private final LoginCredential loginCredential;
  private final NodeType nodeType;
  private final Set<IpAddress> ipAddresses;
  private final String name;
  @Nullable
  private final String diagnostic;
  @Nullable
  private final String reason;
  @Nullable
  private final String nodeCandidate;

  AbstractNodeImpl(NodeProperties nodeProperties, String userId,
      @Nullable LoginCredential loginCredential, NodeType nodeType,
      Set<IpAddress> ipAddresses, String id, String name, @Nullable String diagnostic,
      @Nullable String reason, @Nullable String nodeCandidate) {

    checkNotNull(nodeProperties, "nodeProperties is null");
    checkNotNull(nodeType, "nodeType is null");
    checkNotNull(ipAddresses, "ipAddresses is null");

    this.id = id;
    this.userId = userId;
    this.nodeProperties = nodeProperties;
    this.loginCredential = loginCredential;
    this.nodeType = nodeType;
    this.ipAddresses = ipAddresses;
    this.name = name;
    this.diagnostic = diagnostic;
    this.reason = reason;
    this.nodeCandidate = nodeCandidate;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String userId() {
    return userId;
  }

  @Nullable
  protected static IpAddress returnFirstPingable(Set<IpAddress> addresses) {
    for (IpAddress ipAddress : addresses) {
      if (ipAddress.isPingable()) {
        return ipAddress;
      }
    }
    return null;
  }

  @Nullable
  protected static IpAddress findConnectible(final Set<IpAddress> addresses) {
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
  public Optional<String> diagnostic() {
    return Optional.ofNullable(diagnostic);
  }

  @Override
  public Optional<String> reason() {
    return Optional.ofNullable(reason);
  }

  @Override
  public Optional<String> nodeCandidate() {
    return Optional.ofNullable(nodeCandidate);
  }

  @Override
  public RemoteConnection connect() throws RemoteException {
    return REMOTE_CONNECTION_STRATEGY.connect(this);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ByonNode that = (ByonNode) o;
    return Objects.equals(id, that.id()) &&
        Objects.equals(userId, that.userId()) &&
        Objects.equals(nodeProperties, that.nodeProperties()) &&
        Objects.equals(loginCredential, that.loginCredential()) &&
        Objects.equals(nodeType, that.type()) &&
        Objects.equals(ipAddresses, that.ipAddresses()) &&
        Objects.equals(name, that.name()) &&
        Objects.equals(diagnostic, that.diagnostic()) &&
        Objects.equals(reason, that.reason()) &&
        Objects.equals(nodeCandidate, that.nodeCandidate());
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  protected MoreObjects.ToStringHelper toStringHelper() {
    String ipList = ipAddresses == null ? "null" : Joiner.on(",").join(ipAddresses);
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("userId", userId)
        .add("properties", nodeProperties)
        .add("loginCredential", loginCredential)
        .add("type", nodeType)
        .add("ipAddresses", ipAddresses)
        .add("diagnostic", diagnostic)
        .add("reason", reason)
        .add("nodeCandidate", nodeCandidate);
  }
}
