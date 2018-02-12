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

package io.github.cloudiator.orchestration.installer.tools.installer;


import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import io.github.cloudiator.persistance.TenantModel;
import io.github.cloudiator.orchestration.installer.tools.installer.api.InstallApi;

/**
 * Created by daniel on 05.08.15.
 */
public class Installers {

  private Installers() {

  }

  public static InstallApi of(RemoteConnection remoteConnection, VirtualMachine virtualMachine,
      TenantModel tenantModel) {

    switch (virtualMachine.image().get().operatingSystem().operatingSystemFamily()
        .operatingSystemType()) {
            /*
            case LINUX:
                return new UnixInstaller(remoteConnection, virtualMachine, tenant);
            case WINDOWS:
                return new WindowsInstaller(remoteConnection, virtualMachine, tenant);
                */
      default:
        throw new UnsupportedOperationException(String
            .format("OperatingSystemType %s is not supported by the installation logic",
                virtualMachine.image().get().operatingSystem().operatingSystemFamily()
                    .operatingSystemType()));
    }
  }

}
