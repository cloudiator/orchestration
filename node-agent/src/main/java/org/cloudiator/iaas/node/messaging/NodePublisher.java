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

package org.cloudiator.iaas.node.messaging;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import javax.inject.Inject;
import org.cloudiator.iaas.node.VirtualMachineToNode;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;

public class NodePublisher {

  private final MessageInterface messageInterface;
  private final VirtualMachineToNode virtualMachineToNode;
  private final NodeToNodeMessageConverter nodeConverter = new NodeToNodeMessageConverter();

  @Inject
  public NodePublisher(MessageInterface messageInterface,
      VirtualMachineToNode virtualMachineToNode) {
    this.messageInterface = messageInterface;
    this.virtualMachineToNode = virtualMachineToNode;
  }


  public void publish(
      VirtualMachine virtualMachine, String userId) throws ResponseException {

    final Node node = virtualMachineToNode
        .apply(virtualMachine);

    messageInterface.publish(NodeEvent.newBuilder().setNode(nodeConverter.apply(node)).build());
  }
}
