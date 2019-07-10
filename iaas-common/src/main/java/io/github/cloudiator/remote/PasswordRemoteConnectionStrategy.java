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

package io.github.cloudiator.remote;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.net.HostAndPort;
import de.uniulm.omi.cloudiator.domain.RemoteType;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.sword.domain.PropertiesBuilder;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import de.uniulm.omi.cloudiator.sword.remote.internal.RemoteBuilder;
import de.uniulm.omi.cloudiator.sword.remote.overthere.OverthereModule;
import io.github.cloudiator.domain.BaseNode;

/**
 * Created by daniel on 01.09.15.
 */
public class PasswordRemoteConnectionStrategy implements RemoteConnectionStrategy {


  public PasswordRemoteConnectionStrategy() {
  }

  @Override
  public RemoteConnection connect(BaseNode node)
      throws RemoteException {

    String connectTo = node.connectTo().ip();

    checkNotNull(connectTo, String.format("Node %s has no ip for connection.", node));
    checkState(node.loginCredential().isPresent(),
        String.format("Node %s has no login credential. Can not connect.", node));

    checkState(node.loginCredential().get().password().isPresent(), "No password provided!");
    checkState(node.loginCredential().get().username().isPresent(), "No username provided!");

    checkState(node.nodeProperties().operatingSystem().isPresent(),
        String.format("No operating system known for node %s. Can not connect.", node));

    int remotePort = node.nodeProperties().operatingSystem().get().operatingSystemFamily()
        .operatingSystemType().remotePort();
    RemoteType remoteType = node.nodeProperties().operatingSystem().get().operatingSystemFamily()
        .operatingSystemType().remoteType();

    return RemoteBuilder.newBuilder()
        .remoteModule(new OverthereModule())
        .properties(PropertiesBuilder.newBuilder().build())
        .build().getRemoteConnection(HostAndPort
                .fromParts(connectTo, remotePort),
            remoteType,
            LoginCredentialBuilder.newBuilder()
                .password(node.loginCredential().get().password().get())
                .username(node.loginCredential().get().username().get())
                .build());


  }

  @Override
  public int getPriority() {
    return Priority.LOW;
  }


}
