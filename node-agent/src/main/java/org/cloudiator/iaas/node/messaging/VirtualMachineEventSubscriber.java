/*
 * Copyright (c) 2014-2019 University of Ulm
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

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.persistance.NodeDomainRepository;
import io.github.cloudiator.util.CollectorsUtil;
import org.cloudiator.iaas.node.NodeStateMachine;
import org.cloudiator.messages.Vm.VirtualMachineEvent;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineState;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineEventSubscriber implements Runnable {

  public static final String FAILURE_MESSAGE = "Node %s is affected by failure of vm with id %s. Marking node as failed.";
  private final MessageInterface messageInterface;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeStateMachine nodeStateMachine;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineEventSubscriber.class);

  @Inject
  public VirtualMachineEventSubscriber(MessageInterface messageInterface,
      NodeDomainRepository nodeDomainRepository,
      NodeStateMachine nodeStateMachine) {
    this.messageInterface = messageInterface;
    this.nodeDomainRepository = nodeDomainRepository;
    this.nodeStateMachine = nodeStateMachine;
  }

  @Override
  public void run() {
    messageInterface.subscribe(VirtualMachineEvent.class, VirtualMachineEvent.parser(),
        new MessageCallback<VirtualMachineEvent>() {
          @Override
          public void accept(String id, VirtualMachineEvent content) {

            String userId = content.getUserId();

            if (content.getTo().equals(VirtualMachineState.VM_STATE_ERROR)) {

              LOGGER.info(String
                  .format("Receiving event that virtual machine with id %s has failed.",
                      content.getVm().getId()));

              //search the corresponding node
              final Node affectedNode = nodeDomainRepository.findByTenant(userId).stream()
                  .filter(node -> {
                    if (!node.originId().isPresent()) {
                      return false;
                    }
                    return node.originId().get().equals(content.getVm().getId());
                  }).collect(CollectorsUtil.singletonCollector());

              if (affectedNode == null) {
                LOGGER.info(String.format(
                    "No node is affected by the failure of vm with id %s. Ignoring the event.",
                    content.getVm().getId()));
                return;
              }

              LOGGER.warn(String.format(
                  FAILURE_MESSAGE,
                  affectedNode, content.getVm().getId()));

              //fail the affected node
              nodeStateMachine.fail(affectedNode, new Object[0],
                  new IllegalStateException(
                      String.format(FAILURE_MESSAGE, affectedNode, content.getVm().getId())));


            }
          }
        });
  }
}
