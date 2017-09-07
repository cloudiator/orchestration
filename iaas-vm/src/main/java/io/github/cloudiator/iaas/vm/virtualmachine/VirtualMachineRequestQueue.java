package io.github.cloudiator.iaas.vm.virtualmachine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;

/**
 * Created by daniel on 29.06.17.
 */
class VirtualMachineRequestQueue {

  static class VirtualMachineRequestItem {

    private final String requestId;
    private final VirtualMachineRequest virtualMachineRequest;
    private final String userId;

    private VirtualMachineRequestItem(String requestId,
        VirtualMachineRequest virtualMachineRequest, String userId) {
      checkNotNull(requestId, "requestId is null");
      checkArgument(!requestId.isEmpty(), "requestId is empty");
      this.requestId = requestId;
      checkNotNull(virtualMachineRequest, "virtualMachineRequest is null");
      this.virtualMachineRequest = virtualMachineRequest;
      checkNotNull(userId, "userId is null");
      checkArgument(!userId.isEmpty(), "userId is empty");
      this.userId = userId;
    }

    public String requestId() {
      return requestId;
    }

    public VirtualMachineRequest virtualMachineRequest() {
      return virtualMachineRequest;
    }

    public String userId() {
      return userId;
    }

  }

  private final BlockingQueue<VirtualMachineRequestItem> pendingRequests;

  public VirtualMachineRequestQueue() {
    this.pendingRequests = new LinkedBlockingQueue<>();
  }

  public VirtualMachineRequestItem take() throws InterruptedException {
    return pendingRequests.take();
  }

  public void add(String requestId, CreateVirtualMachineRequestMessage request) {
    pendingRequests.add(new VirtualMachineRequestItem(requestId, request.getVirtualMachineRequest(),
        request.getUserId()));
  }
}
