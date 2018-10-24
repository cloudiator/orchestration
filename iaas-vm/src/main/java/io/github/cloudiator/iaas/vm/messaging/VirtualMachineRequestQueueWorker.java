package io.github.cloudiator.iaas.vm.messaging;

import static jersey.repackaged.com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import io.github.cloudiator.iaas.vm.EnrichVirtualMachine;
import io.github.cloudiator.iaas.vm.VirtualMachineRequestToTemplateConverter;
import io.github.cloudiator.iaas.vm.messaging.VirtualMachineRequestQueue.UserCreateVirtualMachineRequest;
import io.github.cloudiator.iaas.vm.workflow.Exchange;
import io.github.cloudiator.iaas.vm.workflow.CreateVirtualMachineWorkflow;
import io.github.cloudiator.messaging.VirtualMachineMessageToVirtualMachine;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineRequestQueueWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineRequestQueueWorker.class);
  private final VirtualMachineRequestQueue virtualMachineRequestQueue;
  private final MessageInterface messageInterface;
  private final EnrichVirtualMachine enrichVirtualMachine;
  private final VirtualMachineMessageToVirtualMachine vmConverter = VirtualMachineMessageToVirtualMachine.INSTANCE;
  private VirtualMachineRequestToTemplateConverter virtualMachineRequestToTemplateConverter = new VirtualMachineRequestToTemplateConverter();
  private final ComputeService computeService;
  private final VirtualMachineDomainRepository virtualMachineDomainRepository;

  @Inject
  public VirtualMachineRequestQueueWorker(
      VirtualMachineRequestQueue virtualMachineRequestQueue,
      MessageInterface messageInterface,
      EnrichVirtualMachine enrichVirtualMachine,
      ComputeService computeService,
      VirtualMachineDomainRepository virtualMachineDomainRepository) {
    this.virtualMachineRequestQueue = virtualMachineRequestQueue;
    this.messageInterface = messageInterface;
    this.enrichVirtualMachine = enrichVirtualMachine;
    this.computeService = computeService;
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
  }

  @Override
  public void run() {

    while (!Thread.currentThread().isInterrupted()) {

      try {

        UserCreateVirtualMachineRequest userCreateVirtualMachineRequest = null;
        try {
          userCreateVirtualMachineRequest = virtualMachineRequestQueue.take();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        LOGGER.debug(String.format("Starting execution of new virtual machine request %s.",
            userCreateVirtualMachineRequest));

        try {

          checkNotNull(userCreateVirtualMachineRequest, "userCreateVirtualMachineRequest is null");

          VirtualMachineTemplate virtualMachineTemplate = virtualMachineRequestToTemplateConverter
              .apply(userCreateVirtualMachineRequest.virtualMachineRequest());

          LOGGER.debug(String.format("Using virtual machine template %s to start virtual machine.",
              virtualMachineTemplate));

          CreateVirtualMachineWorkflow createVirtualMachineWorkflow = new CreateVirtualMachineWorkflow(
              computeService);

          LOGGER.debug("Starting execution of workflow for virtual machine.");

          Exchange result = createVirtualMachineWorkflow.execute(Exchange.of(virtualMachineTemplate));

          VirtualMachine virtualMachine = result.getData(VirtualMachine.class).get();

          //decorate virtual machine
          final VirtualMachine update = enrichVirtualMachine
              .update(userCreateVirtualMachineRequest.userId(), virtualMachineTemplate,
                  virtualMachine);

          //persist the vm
          persistVirtualMachine(update, userCreateVirtualMachineRequest.userId());

          messageInterface.reply(userCreateVirtualMachineRequest.requestId(),
              VirtualMachineCreatedResponse.newBuilder()
                  .setVirtualMachine(vmConverter.applyBack(update)).build());

        } catch (Exception e) {
          LOGGER.error("Error during execution of virtual machine creation", e);
          messageInterface
              .reply(VirtualMachineCreatedResponse.class,
                  userCreateVirtualMachineRequest.requestId(),
                  Error.newBuilder().setCode(500)
                      .setMessage("Error during creation of virtual machine: " + e.getMessage())
                      .build());
        }

      } catch (Exception e) {
        LOGGER.warn(String
                .format("Uncaught error %s during execution of worker. Caught to resume operation.",
                    e.getMessage()),
            e);
      }
    }


  }

  @Transactional
  void persistVirtualMachine(VirtualMachine vm, String userId) {
    virtualMachineDomainRepository.save(vm, userId);
  }
}
