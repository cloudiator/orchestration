/*
 * Copyright (c) 2014-2015 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.orchestration.installer.remote;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.net.HostAndPort;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.domain.RemoteType;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.sword.domain.PropertiesBuilder;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import de.uniulm.omi.cloudiator.sword.remote.internal.RemoteBuilder;
import de.uniulm.omi.cloudiator.sword.remote.overthere.OverthereModule;
import org.cloudiator.messages.NodeOuterClass.Node;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.IpAddressType;

/**
 * Created by daniel on 01.09.15.
 */
public class PasswordRemoteConnectionStrategy implements RemoteConnectionStrategy {

    public PasswordRemoteConnectionStrategy() {
    }

    @Override public RemoteConnection connect(Node node, OperatingSystem operatingSystem)
        throws RemoteException {

        //TODO: check if still required
        //checkState(virtualMachine.owner().isPresent(),
          //  "Owner of virtual machine should be set before calling connect.");


      //check public Ip(s)


      String publicIp = null;
      for (IaasEntities.IpAddress ipAddress:node.getIpAddressesList()
      ) {
        if(ipAddress.getType().equals(IpAddressType.PUBLIC_IP)){
          publicIp = ipAddress.getIp();
        }
      }

      checkNotNull(publicIp, "No publicIps set! Virtual machine must have a public ip address.");


      //final Optional<String> anyPublicAddress =
        //  virtualMachine.publicAddresses().stream().findAny();

      //checkArgument(anyPublicAddress.isPresent(),
        //  "Virtual machine must have a public ip address.");

      /*
        if (!virtualMachine.loginCredential().get().password().isPresent()) {
            throw new RemoteException(String
                .format("Virtual machine %s does not provide a login password", virtualMachine));
        }
    */

      checkState(node.getLoginCredential().getPassword().isEmpty(),"No password provided!");


      int remotePort = operatingSystem.operatingSystemFamily().operatingSystemType().remotePort();
      RemoteType remoteType = operatingSystem.operatingSystemFamily().operatingSystemType().remoteType();



      //TODO: check if required
      // Map<String, Object> properties =
      //      new CompositeCloudPropertyProvider(virtualMachine.cloud()).properties();

      return RemoteBuilder.newBuilder()
          .remoteModule(new OverthereModule())
          .properties(PropertiesBuilder.newBuilder().build())
          .build().getRemoteConnection( HostAndPort
          .fromParts(publicIp, remotePort),
          remoteType,
          LoginCredentialBuilder.newBuilder()
              .password(node.getLoginCredential().getPassword())
              .username(node.getLoginCredential().getUsername())
              .build());


    }

    @Override public int getPriority() {
        return Priority.LOW;
    }



}
