package io.github.cloudiator.iaas.vm.messaging;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import io.github.cloudiator.iaas.vm.workflow.DeleteVirtualMachineWorkflow;
import io.github.cloudiator.iaas.vm.workflow.Exchange;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.DeleteVirtualMachineRequestMessage;
import org.cloudiator.messages.Vm.VirtualMachineDeletedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineDeleteRequestSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineDeleteRequestSubscriber.class);
  private final MessageInterface messageInterface;
  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final ComputeService computeService;

  @Inject
  public VirtualMachineDeleteRequestSubscriber(
      MessageInterface messageInterface,
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      ComputeService computeService) {
    this.messageInterface = messageInterface;
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.computeService = computeService;
  }

  void deleteVm(String vmId, String userId) {
    virtualMachineDomainRepository.delete(vmId, userId);
  }

  @Override
  public void run() {
    messageInterface.subscribe(DeleteVirtualMachineRequestMessage.class,
        DeleteVirtualMachineRequestMessage.parser(),
        new MessageCallback<DeleteVirtualMachineRequestMessage>() {
          @Override
          public void accept(String id, DeleteVirtualMachineRequestMessage content) {

            try {
              final String userId = content.getUserId();
              final String vmId = content.getVmId();

              LOGGER.debug(String
                  .format("%s retrieved request to delete vm with id %s for user %s.", this, vmId,
                      userId));

              VirtualMachine byTenantAndId = virtualMachineDomainRepository
                  .findByTenantAndId(userId, vmId);

              if (byTenantAndId == null) {
                messageInterface.reply(VirtualMachineDeletedResponse.class, id,
                    Error.newBuilder().setCode(404).setMessage(
                        String.format("VirtualMachine with id %s does not exist.", vmId)).build());
                return;
              }

              LOGGER.info(String
                  .format("%s is executing the delete virtual machine workflow for vm %s.", this,
                      byTenantAndId));

              new DeleteVirtualMachineWorkflow(computeService).execute(Exchange.of(vmId));

              deleteVm(vmId, userId);

              LOGGER.info(String
                  .format("%s successfully deleted vm %s.", this,
                      byTenantAndId));

              messageInterface.reply(id, VirtualMachineDeletedResponse.newBuilder().build());


            } catch (Exception e) {
              LOGGER.error("Unexpected exception while deleting virtual machine: " + e.getMessage(),
                  e);
              messageInterface.reply(VirtualMachineDeletedResponse.class, id,
                  Error.newBuilder().setCode(500).setMessage(
                      "Unexpected exception while deleting virtual machine: " + e.getMessage())
                      .build());
            }


          }
        });
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
