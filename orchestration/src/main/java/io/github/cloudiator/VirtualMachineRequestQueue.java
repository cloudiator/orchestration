package io.github.cloudiator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestRequest;

/**
 * Created by daniel on 29.06.17.
 */
public class VirtualMachineQueue {

  private final BlockingQueue<CreateVirtualMachineRequestRequest> pendingRequests;

  public VirtualMachineQueue(
      BlockingQueue<CreateVirtualMachineRequestRequest> pendingRequests) {
    this.pendingRequests = new LinkedBlockingQueue<>();
  }

  public CreateVirtualMachineRequestRequest take() throws InterruptedException {
    return pendingRequests.take();
  }

  public void add(CreateVirtualMachineRequestRequest request) {
    pendingRequests.add(request);
  }


}
