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

package io.github.cloudiator.iaas.vm.messaging;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.messaging.VirtualMachineMessageToVirtualMachine;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.VirtualMachineQueryMessage;
import org.cloudiator.messages.Vm.VirtualMachineQueryResponse;
import org.cloudiator.messages.Vm.VirtualMachineQueryResponse.Builder;
import org.cloudiator.messaging.MessageInterface;

public class VirtualMachineQuerySubscriber implements Runnable {

  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final MessageInterface messageInterface;
  private static final VirtualMachineMessageToVirtualMachine VM_CONVERTER = VirtualMachineMessageToVirtualMachine.INSTANCE;

  @Transactional
  List<ExtendedVirtualMachine> findAll(String userId) {
    return virtualMachineDomainRepository.findAll(userId);
  }

  @Transactional
  List<ExtendedVirtualMachine> findByCloud(String userId, String cloudId) {
    return virtualMachineDomainRepository.findByCloud(userId, cloudId);
  }

  @Transactional
  ExtendedVirtualMachine findById(String id, String userId) {
    return virtualMachineDomainRepository.findByTenantAndId(userId, id);
  }

  @Inject
  public VirtualMachineQuerySubscriber(
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      MessageInterface messageInterface) {
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {

    messageInterface.subscribe(VirtualMachineQueryMessage.class,
        VirtualMachineQueryMessage.parser(), (id, content) -> {

          try {
            final String userId = Strings.emptyToNull(content.getUserId());
            final String vmId = Strings.emptyToNull(content.getVmId());
            final String cloudId = Strings.emptyToNull(content.getCloudId());

            checkArgument(userId != null, "user id is null");

            final Builder builder = VirtualMachineQueryResponse.newBuilder();
            if (vmId != null) {
              checkArgument(cloudId == null, "cloud id is not null but vm id was supplied");
              //reply for single vm
              final ExtendedVirtualMachine byId = findById(vmId, userId);
              if (byId != null) {
                builder.addVirtualMachines(VM_CONVERTER.applyBack(byId));
              }
            } else {
              if (cloudId != null) {
                //reply with cloud filter
                builder.addAllVirtualMachines(
                    findByCloud(userId, cloudId).stream().map(VM_CONVERTER::applyBack).collect(
                        Collectors.toList()));
              } else {
                //reply all vms
                builder.addAllVirtualMachines(
                    findAll(userId).stream().map(VM_CONVERTER::applyBack).collect(
                        Collectors.toList()));
              }
            }

            messageInterface.reply(id,
                builder.build());


          } catch (Exception e) {
            messageInterface.reply(VirtualMachineQueryMessage.class, id,
                Error.newBuilder().setCode(500)
                    .setMessage("Error while querying vms: " + e.getMessage()).build());
          }


        });
  }
}
