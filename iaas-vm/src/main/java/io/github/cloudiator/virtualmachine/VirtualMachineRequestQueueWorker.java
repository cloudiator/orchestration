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

  @Inject
  public VirtualMachineRequestQueueWorker(
      VirtualMachineRequestQueue virtualMachineRequestQueue,
      CloudService cloudService,
      NodePublisher nodePublisher) {
    this.virtualMachineRequestQueue = virtualMachineRequestQueue;
    this.cloudService = cloudService;
    this.nodePublisher = nodePublisher;
  }

  @Override
  public void run() {

    while (!Thread.currentThread().isInterrupted()) {
      try {
        VirtualMachineRequestItem virtualMachineRequestItem = virtualMachineRequestQueue.take();
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
            .publish(virtualMachineTemplate.imageId(), virtualMachineTemplate.hardwareFlavorId(),
                virtualMachineTemplate.locationId(), virtualMachine,
                virtualMachineRequestItem.userId());


      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        LOGGER.error("Error during execution of virtual machine creation", e);
      }

    }


  }
}
