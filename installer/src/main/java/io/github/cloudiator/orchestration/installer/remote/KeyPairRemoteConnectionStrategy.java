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
import de.uniulm.omi.cloudiator.util.execution.Prioritized;
import io.github.cloudiator.iaas.common.messaging.OperatingSystemConverter;
import org.cloudiator.messages.NodeOuterClass.Node;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.IpAddressType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 31.08.15.
 */
public class KeyPairRemoteConnectionStrategy implements RemoteConnectionStrategy {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(KeyPairRemoteConnectionStrategy.class);

    public KeyPairRemoteConnectionStrategy() {
    }

    @Override public RemoteConnection connect(Node node, OperatingSystem operatingSystem)
        throws RemoteException {

        //final Optional<String> anyPublicAddress =
          //  virtualMachine.publicAddresses().stream().findAny();

        //checkArgument(anyPublicAddress.isPresent(),
          //  "Virtual machine must have a public ip address.");

        //checkArgument(virtualMachine.loginCredential().isPresent(),
          //  "Virtual machine must have login credentials available.");

        String publicIp = null;
        for (IaasEntities.IpAddress ipAddress:node.getIpAddressesList()
            ) {
            if(ipAddress.getType().equals(IpAddressType.PUBLIC_IP)){
                publicIp = ipAddress.getIp();
            }
        }
        checkNotNull(publicIp, "No publicIps set! Virtual machine must have a public ip address.");

        /*
        String privateKey = virtualMachine.loginCredential().get().privateKey()
            .orElseThrow(new Supplier<RemoteException>() {
                @Override public RemoteException get() {
                    return new RemoteException(String
                        .format("%s could not retrieve a key pair for virtual machine %s", this,
                            virtualMachine));
                }
            });
         */

      String privateKey = node.getLoginCredential().getPrivateKey();
      checkState(!privateKey.isEmpty(),"Could not retrieve a key pair for node!");

      LOGGER.debug(privateKey);

        int remotePort = operatingSystem.operatingSystemFamily().operatingSystemType().remotePort();
        RemoteType remoteType =operatingSystem.operatingSystemFamily().operatingSystemType().remoteType();

        //get username from credential or OS
        String userName;
        if(!node.getLoginCredential().getUsername().isEmpty()){
          userName = node.getLoginCredential().getUsername();
        }else{

          OperatingSystemConverter operatingSystemConverter = new OperatingSystemConverter();
          OperatingSystem operatingSystemDomain = operatingSystemConverter.apply(node.getNodeProperties().getOperationSystem());

          userName = operatingSystemDomain.operatingSystemFamily().loginName();
        }

        return RemoteBuilder.newBuilder()
            .remoteModule(new OverthereModule())
            .properties(PropertiesBuilder.newBuilder().build())
            .build().getRemoteConnection( HostAndPort
                    .fromParts(publicIp, remotePort),
                remoteType,
                LoginCredentialBuilder.newBuilder()
                    .privateKey(privateKey)
                    .username(userName)
                    .build());
    }

    @Override public int getPriority() {
        return Prioritized.Priority.HIGH;
    }

}
