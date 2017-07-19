package io.github.cloudiator.virtualmachine;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;

/**
 * Created by daniel on 29.06.17.
 */
public class CreateVirtualMachineSubscriber implements Runnable {

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
        CreateVirtualMachineRequestMessage.parser(), virtualMachineRequestQueue::add);
  }
}
