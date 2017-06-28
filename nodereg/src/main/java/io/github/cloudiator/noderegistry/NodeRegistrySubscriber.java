package io.github.cloudiator.noderegistry;

import javax.inject.Inject;

import org.cloudiator.messages.Vm.VirtualMachineEvent;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeRegistrySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistrySubscriber.class);

  private final MessageInterface messagingService;
  private final NodeRegistry registry;
  private volatile Subscription subscription;

  @Inject
  public NodeRegistrySubscriber(MessageInterface messageInterface, CloudService cloudService,
      NodeRegistry registry) {
    this.messagingService = messageInterface;
    this.registry = registry;
  }

  @Override
  public void run() {
    subscription = messagingService.subscribe(VirtualMachineEvent.class,
        VirtualMachineEvent.parser(), (eventId, virtualMachineEvent) -> {

          if (null == virtualMachineEvent.getVmStatus()) {
            throw new IllegalArgumentException("status not set: null");
          }

          try {
            switch (virtualMachineEvent.getVmStatus()) {
              case CREATED:
                handleCreation(virtualMachineEvent.getVirtualMachine());
                break;
              case DELETED:
                handleDeletion(virtualMachineEvent.getVirtualMachine());
                break;
              case UNDEFINED:
                throw new IllegalArgumentException(
                    "status not set: " + virtualMachineEvent.getVmStatus());
              default:
                throw new IllegalArgumentException(
                    "status unknown: " + virtualMachineEvent.getVmStatus());
            }
          } catch (Exception ex) {
            LOGGER.error("exception occurred when handling virtual machine event.", ex);
          }
        });
  }

  synchronized void handleDeletion(VirtualMachine virtualMachine) throws RegistryException {
    String vmId = virtualMachine.getId();
    registry.remove(vmId);
  }

  synchronized void handleCreation(VirtualMachine virtualMachine) throws RegistryException {
    String vmId = virtualMachine.getId();
    registry.put(vmId, virtualMachine);
  }

  void terminate() {
    if (subscription != null) {
      subscription.cancel();
    }
  }
}
