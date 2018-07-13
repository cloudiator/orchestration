package io.github.cloudiator.iaas.vm;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 29.06.17.
 */
public class CreateVirtualMachineSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CreateVirtualMachineSubscriber.class);
  private final MessageInterface messageInterface;
  private final VirtualMachineRequestQueue virtualMachineRequestQueue;


  @Inject
  public CreateVirtualMachineSubscriber(MessageInterface messageInterface,
      VirtualMachineRequestQueue virtualMachineRequestQueue) {
    checkNotNull(virtualMachineRequestQueue, "virtualMachineQueue is null");
    this.virtualMachineRequestQueue = virtualMachineRequestQueue;
    checkNotNull(messageInterface, "messageInterface is null");
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    Subscription subscription = messageInterface.subscribe(CreateVirtualMachineRequestMessage.class,
        CreateVirtualMachineRequestMessage.parser(),
        (requestId, request) -> {
          LOGGER.info(String
              .format("%s is receiving new request for virtual machine %s. Adding to queue.", this,
                  request));
          virtualMachineRequestQueue.add(requestId, request);
        });
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
