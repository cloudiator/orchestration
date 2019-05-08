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

package org.cloudiator.iaas.node;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodeCandidateType;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.messaging.NodeCandidateMessageRepository;
import io.github.cloudiator.messaging.VirtualMachineMessageToVirtualMachine;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.VirtualMachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineNodeSchedulingStrategy implements NodeSchedulingStrategy {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineNodeSchedulingStrategy.class);

  private static final VirtualMachineMessageToVirtualMachine VIRTUAL_MACHINE_CONVERTER = VirtualMachineMessageToVirtualMachine.INSTANCE;
  private final VirtualMachineService virtualMachineService;
  private final NodeCandidateMessageRepository nodeCandidateMessageRepository;

  @Inject
  public VirtualMachineNodeSchedulingStrategy(VirtualMachineService virtualMachineService,
      NodeCandidateMessageRepository nodeCandidateMessageRepository) {

    this.virtualMachineService = virtualMachineService;
    this.nodeCandidateMessageRepository = nodeCandidateMessageRepository;
  }

  private NodeCandidate retrieveCandidate(Node pending) {
    checkState(pending.nodeCandidate().isPresent(), "nodeCandidate is not present in pending node");

    final NodeCandidate nodeCandidate = nodeCandidateMessageRepository
        .getById(pending.userId(), pending.nodeCandidate().get());

    checkNotNull(nodeCandidate, String
        .format("NodeCandidate with id %s does no (longer) exist.", pending.nodeCandidate().get()));

    return nodeCandidate;
  }

  @Override
  public boolean canSchedule(Node pending) {
    return retrieveCandidate(pending).type().equals(NodeCandidateType.IAAS);
  }

  public Node schedule(Node pending) throws NodeSchedulingException {

    final SettableFutureResponseCallback<VirtualMachineCreatedResponse, VirtualMachine> virtualMachineFuture = SettableFutureResponseCallback
        .create(
            virtualMachineCreatedResponse -> VIRTUAL_MACHINE_CONVERTER
                .apply(virtualMachineCreatedResponse.getVirtualMachine()));

    final NodeCandidate nodeCandidate = retrieveCandidate(pending);

    final VirtualMachineRequest virtualMachineRequest = generateRequest(pending, nodeCandidate);
    CreateVirtualMachineRequestMessage createVirtualMachineRequestMessage = CreateVirtualMachineRequestMessage
        .newBuilder().setUserId(pending.userId()).setVirtualMachineRequest(virtualMachineRequest)
        .build();

    LOGGER.debug(String
        .format("%s is sending virtual machine request %s for node candidate %s.", this,
            virtualMachineRequest, nodeCandidate));

    virtualMachineService
        .createVirtualMachineAsync(createVirtualMachineRequestMessage, virtualMachineFuture);

    try {
      final VirtualMachine virtualMachine = virtualMachineFuture.get();

      LOGGER.debug(String
          .format("%s incarnated nodeCandidate %s as virtual machine %s.", this, nodeCandidate,
              virtualMachine));

      return NodeBuilder.of(virtualMachine).state(NodeState.RUNNING).userId(pending.userId())
          .nodeCandidate(nodeCandidate.id()).id(pending.id()).name(pending.name()).build();

    } catch (InterruptedException e) {
      throw new IllegalStateException("Got interrupted while waiting for virtual machine to start",
          e);
    } catch (ExecutionException e) {
      throw new NodeSchedulingException(String
          .format("Could not schedule node %s due to exception: %s.", pending,
              e.getCause().getMessage()), e);
    }
  }

  private VirtualMachineRequest generateRequest(Node pending, NodeCandidate nodeCandidate) {

    return VirtualMachineRequest.newBuilder()
        .setHardware(nodeCandidate.hardware().id())
        .setImage(nodeCandidate.image().id())
        .setLocation(nodeCandidate.location().id())
        .setName(pending.name())
        .build();
  }
}
