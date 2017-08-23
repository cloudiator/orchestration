package io.github.cloudiator.virtualmachine;

import static jersey.repackaged.com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import io.github.cloudiator.virtualmachine.VirtualMachineRequestQueue.VirtualMachineRequestItem;
import io.github.cloudiator.workflow.Exchange;
import io.github.cloudiator.workflow.VirtualMachineWorkflow;
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
  private final NodePublisher nodePublisher;
  private final MessageInterface messageInterface;

  @Inject
  public VirtualMachineRequestQueueWorker(
      VirtualMachineRequestQueue virtualMachineRequestQueue,
      CloudService cloudService,
      NodePublisher nodePublisher, MessageInterface messageInterface) {
    this.virtualMachineRequestQueue = virtualMachineRequestQueue;
    this.cloudService = cloudService;
    this.nodePublisher = nodePublisher;
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {

    while (!Thread.currentThread().isInterrupted()) {

      VirtualMachineRequestItem virtualMachineRequestItem = null;
      try {
        virtualMachineRequestItem = virtualMachineRequestQueue.take();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      try {

        VirtualMachineTemplate virtualMachineTemplate = virtualMachineRequestToTemplateConverter
            .apply(virtualMachineRequestItem.virtualMachineRequest());

        CloudRetrieval cloudRetrieval = new CloudRetrieval(cloudService);
        Cloud cloud = cloudRetrieval.retrieve(CloudId.instance().apply(virtualMachineTemplate),
            virtualMachineRequestItem.userId());

        checkNotNull(cloud, "cloud is null");

        MultiCloudService multiCloudService = MultiCloudBuilder.newBuilder().build();
        multiCloudService.cloudRegistry().register(cloud);

        VirtualMachineWorkflow virtualMachineWorkflow = new VirtualMachineWorkflow(
            multiCloudService.computeService());
        Exchange result = virtualMachineWorkflow.execute(Exchange.of(virtualMachineTemplate));

        VirtualMachine virtualMachine = result.getData(VirtualMachine.class).get();

        nodePublisher
            .publish(virtualMachine,
                virtualMachineRequestItem.userId());

      } catch (Exception e) {
        messageInterface
            .reply(VirtualMachineCreatedResponse.class, virtualMachineRequestItem.requestId(),
                Error.newBuilder().setCode(500).setMessage(e.getMessage()).build());
        LOGGER.error("Error during execution of virtual machine creation", e);
      }

    }


  }
}
