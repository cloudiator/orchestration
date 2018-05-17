package io.github.cloudiator.iaas.vm;

import static jersey.repackaged.com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import io.github.cloudiator.iaas.vm.VirtualMachineRequestQueue.UserCreateVirtualMachineRequest;
import io.github.cloudiator.iaas.vm.workflow.Exchange;
import io.github.cloudiator.iaas.vm.workflow.VirtualMachineWorkflow;
import io.github.cloudiator.messaging.CloudMessageRepository;
import io.github.cloudiator.messaging.VirtualMachineMessageToVirtualMachine;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineRequestQueueWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineRequestQueueWorker.class);
  private final VirtualMachineRequestQueue virtualMachineRequestQueue;
  private final CloudService cloudService;
  private final MessageInterface messageInterface;
  private final UpdateVirtualMachine updateVirtualMachine;
  private final VirtualMachineMessageToVirtualMachine vmConverter = new VirtualMachineMessageToVirtualMachine();
  private VirtualMachineRequestToTemplateConverter virtualMachineRequestToTemplateConverter = new VirtualMachineRequestToTemplateConverter();

  @Inject
  public VirtualMachineRequestQueueWorker(
      VirtualMachineRequestQueue virtualMachineRequestQueue,
      CloudService cloudService,
      MessageInterface messageInterface,
      UpdateVirtualMachine updateVirtualMachine) {
    this.virtualMachineRequestQueue = virtualMachineRequestQueue;
    this.cloudService = cloudService;
    this.messageInterface = messageInterface;
    this.updateVirtualMachine = updateVirtualMachine;
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

          CloudMessageRepository cloudMessageRepository = new CloudMessageRepository(cloudService);
          Cloud cloud = cloudMessageRepository.getById(userCreateVirtualMachineRequest.userId(),
              CloudId.instance().apply(virtualMachineTemplate));

          MultiCloudService multiCloudService = MultiCloudBuilder.newBuilder().build();
          multiCloudService.cloudRegistry().register(cloud);

          VirtualMachineWorkflow virtualMachineWorkflow = new VirtualMachineWorkflow(
              multiCloudService.computeService());

          LOGGER.debug("Starting execution of workflow for virtual machine.");

          Exchange result = virtualMachineWorkflow.execute(Exchange.of(virtualMachineTemplate));

          VirtualMachine virtualMachine = result.getData(VirtualMachine.class).get();

          //decorate virtual machine
          final VirtualMachine update = updateVirtualMachine
              .update(userCreateVirtualMachineRequest.userId(), virtualMachineTemplate,
                  virtualMachine);

          messageInterface.reply(userCreateVirtualMachineRequest.requestId(),
              VirtualMachineCreatedResponse.newBuilder()
                  .setVirtualMachine(vmConverter.applyBack(update)).build());

        } catch (Exception e) {
          LOGGER.error("Error during execution of virtual machine creation", e);
          messageInterface
              .reply(VirtualMachineCreatedResponse.class,
                  userCreateVirtualMachineRequest.requestId(),
                  Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
        }

      } catch (Exception e) {
        LOGGER.warn(String
                .format("Uncaught error %e during execution of worker. Caught to resume operation.", e),
            e.getMessage());
      }
    }


  }
}
