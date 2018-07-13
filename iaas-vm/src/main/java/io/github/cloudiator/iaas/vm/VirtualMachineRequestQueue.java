package io.github.cloudiator.iaas.vm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestMessage;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 29.06.17.
 */
public class VirtualMachineRequestQueue {

  private final BlockingQueue<UserCreateVirtualMachineRequest> pendingRequests;

  public VirtualMachineRequestQueue() {
    this.pendingRequests = new LinkedBlockingQueue<>();
  }

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineRequestQueue.class);

  UserCreateVirtualMachineRequest take() throws InterruptedException {
    return pendingRequests.take();
  }

  void add(String requestId, CreateVirtualMachineRequestMessage request) {
    LOGGER.debug(String
        .format("New request %s was added to %s. Currently %s requests pending.", request, this,
            pendingRequests.size()));
    pendingRequests
        .add(new UserCreateVirtualMachineRequest(requestId, request.getVirtualMachineRequest(),
            request.getUserId()));
  }

  static class UserCreateVirtualMachineRequest {

    private final String requestId;
    private final VirtualMachineRequest virtualMachineRequest;
    private final String userId;

    private UserCreateVirtualMachineRequest(String requestId,
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

    String requestId() {
      return requestId;
    }

    VirtualMachineRequest virtualMachineRequest() {
      return virtualMachineRequest;
    }

    String userId() {
      return userId;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("requestId", requestId)
          .add("virtualMachineRequest", virtualMachineRequest).add("userId", userId).toString();
    }

  }
}
