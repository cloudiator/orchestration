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

package org.cloudiator.orchestration.installer.remote;

import com.google.common.net.HostAndPort;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.api.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.api.remote.RemoteException;
import de.uniulm.omi.cloudiator.util.execution.Prioritized;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by daniel on 31.08.15.
 */
public class KeyPairRemoteConnectionStrategy implements RemoteConnectionStrategy {

    private KeyPairRemoteConnectionStrategy() {
    }

    @Override public RemoteConnection connect(VirtualMachine virtualMachine)
        throws RemoteException {

        final Optional<String> anyPublicAddress =
            virtualMachine.publicAddresses().stream().findAny();

        checkArgument(anyPublicAddress.isPresent(),
            "Virtual machine must have a public ip address.");

        checkArgument(virtualMachine.loginCredential().isPresent(),
            "Virtual machine must have login credentials available.");


        String privateKey = virtualMachine.loginCredential().get().privateKey()
            .orElseThrow(new Supplier<RemoteException>() {
                @Override public RemoteException get() {
                    return new RemoteException(String
                        .format("%s could not retrieve a key pair for virtual machine %s", this,
                            virtualMachine));
                }
            });

        Map<String, Object> properties =
            new CompositeCloudPropertyProvider(virtualMachine.cloud()).properties();

        return new DefaultSwordConnectionService(
            RemoteBuilder.newBuilder().loggingModule(new SwordLoggingModule())
                .remoteModule(new OverthereModule())
                .properties(PropertiesBuilder.newBuilder().putProperties(properties).build())
                .build()).getRemoteConnection(HostAndPort
                .fromParts(virtualMachine.publicIpAddress().get().getIp(), virtualMachine.remotePort()),
            virtualMachine.operatingSystem().operatingSystemFamily().operatingSystemType()
                .remoteType(),
            LoginCredentialBuilder.newBuilder().username(virtualMachine.loginName())
                .privateKey(privateKey).build());
    }

    @Override public int getPriority() {
        return Prioritized.Priority.HIGH;
    }

    public static class KeyPairRemoteConnectionStrategyFactory
        implements RemoteConnectionStrategyFactory {

        @Inject public KeyPairRemoteConnectionStrategyFactory() {
        }

        @Override public RemoteConnectionStrategy create() {
            return new KeyPairRemoteConnectionStrategy();
        }
    }
}
