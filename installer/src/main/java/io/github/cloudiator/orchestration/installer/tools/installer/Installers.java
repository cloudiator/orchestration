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

package io.github.cloudiator.orchestration.installer.tools.installer;


import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.orchestration.installer.tools.installer.api.InstallApi;

/**
 * Created by daniel on 05.08.15.
 */
public class Installers {

  private Installers() {

  }

  public static InstallApi of(RemoteConnection remoteConnection, Node node, String userId) {

    switch (node.nodeProperties().operatingSystem().get()
        .operatingSystemFamily().operatingSystemType()) {

      case LINUX:
        return new UnixInstaller(remoteConnection, node, userId);
      case WINDOWS:
        return new WindowsInstaller(remoteConnection, node, userId);

      default:
        throw new UnsupportedOperationException(String
            .format("OperatingSystemType %s is not supported by the installation logic",
                node.nodeProperties().operatingSystem().get()
                    .operatingSystemFamily().operatingSystemType()));
    }
  }

}
