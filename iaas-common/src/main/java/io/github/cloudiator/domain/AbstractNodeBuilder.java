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

import de.uniulm.omi.cloudiator.domain.LoginNameSupplier.UnknownLoginNameException;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.util.NameGenerator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractNodeBuilder<T extends AbstractNodeBuilder<T>> {

  private static final NameGenerator NAME_GENERATOR = NameGenerator.INSTANCE;
  protected String id;
  protected String userId;
  protected NodeProperties nodeProperties;
  protected LoginCredential loginCredential;
  protected NodeType nodeType;
  protected Set<IpAddress> ipAddresses;
  protected String name;
  protected String diagnostic;
  protected String reason;
  protected String nodeCandidate;

  protected AbstractNodeBuilder() {
    this.ipAddresses = new HashSet<>();
  }

  protected AbstractNodeBuilder(BaseNode node) {
    id = node.id();
    userId = node.userId();
    nodeProperties = node.nodeProperties();
    loginCredential = node.loginCredential().orElse(null);
    nodeType = node.type();
    ipAddresses = node.ipAddresses();
    name = node.name();
    diagnostic = node.diagnostic().orElse(null);
    reason = node.reason().orElse(null);
    nodeCandidate = node.nodeCandidate().orElse(null);
  }

  protected AbstractNodeBuilder(VirtualMachine virtualMachine, NodeType nodeType) {

    final String providerId = IdScopedByClouds.from(virtualMachine.id()).cloudId();

    final NodeProperties nodeProperties = NodePropertiesBuilder
        .of(providerId, virtualMachine.hardware().orElse(null), virtualMachine.image().orElse(null),
            virtualMachine.location().orElse(null))
        .build();

    LoginCredential loginCredential = null;

    if (virtualMachine.loginCredential().isPresent()) {
      loginCredential = virtualMachine.loginCredential().get();
      if (!loginCredential.username().isPresent()) {
        if (virtualMachine.image().isPresent()) {
          final OperatingSystem operatingSystem = virtualMachine.image().get().operatingSystem();
          try {
            final String loginName = operatingSystem.operatingSystemFamily().loginName();
            loginCredential = LoginCredentialBuilder.of(loginCredential).username(loginName)
                .build();
          } catch (UnknownLoginNameException ignored) {
            //left empty
          }
        }
      }
    }

    this.nodeType = nodeType;
    ipAddresses = virtualMachine.ipAddresses();
    this.loginCredential = loginCredential;
    this.nodeProperties = nodeProperties;
  }

  public T generateId() {
    this.id = UUID.randomUUID().toString();
    return self();
  }

  public T id(String id) {
    this.id = id;
    return self();
  }

  public T userId(String userId) {
    this.userId = userId;
    return self();
  }

  public T nodeProperties(
      NodeProperties nodeProperties) {
    this.nodeProperties = nodeProperties;
    return self();
  }

  public T loginCredential(
      LoginCredential loginCredential) {
    this.loginCredential = loginCredential;
    return self();
  }

  public T ipAddresses(
      Set<IpAddress> ipAddresses) {
    this.ipAddresses = ipAddresses;
    return self();
  }

  public T name(String name) {
    this.name = name;
    return self();
  }

  public T generateName(String groupName) {
    this.name = NAME_GENERATOR.generate(groupName);
    return self();
  }

  public T diagnostic(String diagnostic) {
    this.diagnostic = diagnostic;
    return self();
  }

  public T reason(String reason) {
    this.reason = reason;
    return self();
  }

  public T nodeCandidate(String nodeCandidate) {
    this.nodeCandidate = nodeCandidate;
    return self();
  }

  protected abstract T self();

  public abstract T nodeType(NodeType nodeType);

  public abstract BaseNode build();
}
