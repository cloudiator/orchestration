package io.github.cloudiator.iaas.vm;

import static jersey.repackaged.com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import io.github.cloudiator.iaas.common.messaging.converters.VirtualMachineMessageToVirtualMachine;
import io.github.cloudiator.iaas.common.messaging.domain.CloudMessageRepository;
import io.github.cloudiator.iaas.vm.VirtualMachineRequestQueue.UserCreateVirtualMachineRequest;
import io.github.cloudiator.iaas.vm.workflow.Exchange;
import io.github.cloudiator.iaas.vm.workflow.VirtualMachineWorkflow;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineRequestQueueWorker implements Runnable {

  private final VirtualMachineRequestQueue virtualMachineRequestQueue;
  private final CloudService cloudService;
  private VirtualMachineRequestToTemplateConverter virtualMachineRequestToTemplateConverter = new VirtualMachineRequestToTemplateConverter();
  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineRequestQueueWorker.class);
  private final MessageInterface messageInterface;
  private final UpdateVirtualMachine updateVirtualMachine;
  private final VirtualMachineMessageToVirtualMachine vmConverter = new VirtualMachineMessageToVirtualMachine();

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

      UserCreateVirtualMachineRequest userCreateVirtualMachineRequest = null;
      try {
        userCreateVirtualMachineRequest = virtualMachineRequestQueue.take();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      try {

        checkNotNull(userCreateVirtualMachineRequest, "userCreateVirtualMachineRequest is null");

        VirtualMachineTemplate virtualMachineTemplate = virtualMachineRequestToTemplateConverter
            .apply(userCreateVirtualMachineRequest.virtualMachineRequest());

        CloudMessageRepository cloudMessageRepository = new CloudMessageRepository(cloudService);
        Cloud cloud = cloudMessageRepository.getById(userCreateVirtualMachineRequest.userId(),
            CloudId.instance().apply(virtualMachineTemplate));

        MultiCloudService multiCloudService = MultiCloudBuilder.newBuilder().build();
        multiCloudService.cloudRegistry().register(cloud);

        VirtualMachineWorkflow virtualMachineWorkflow = new VirtualMachineWorkflow(
            multiCloudService.computeService());
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
        messageInterface
            .reply(VirtualMachineCreatedResponse.class, userCreateVirtualMachineRequest.requestId(),
                Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
        LOGGER.error("Error during execution of virtual machine creation", e);
      }

    }


  }
}
