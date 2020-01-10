/*
 * Copyright (c) 2014-2020 University of Ulm
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

package org.cloudiator.iaas.node.check;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeType;

/**
 * Checks a node
 */
public class NodeCheck {

  private final Node node;

  private NodeCheck(Node node) {
    this.node = node;
  }

  public static NodeCheck forNode(Node node) {
    return new NodeCheck(node);
  }

  public boolean execute() {
    return sshConnectivity();
  }

  private boolean sshConnectivity() {

    if (!node.type().equals(NodeType.VM)) {
      return true;
    }

    try (RemoteConnection remoteConnection = node.connect()) {
      return true;
    } catch (RemoteException e) {
      return false;
    }
  }


}
