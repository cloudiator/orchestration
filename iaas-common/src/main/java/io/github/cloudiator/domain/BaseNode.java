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

import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpAddressType;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public interface BaseNode extends Identifiable {

  @Override
  String id();

  String userId();

  String name();

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

  Optional<String> diagnostic();

  Optional<String> reason();

  Optional<String> nodeCandidate();

  RemoteConnection connect() throws RemoteException;
}
