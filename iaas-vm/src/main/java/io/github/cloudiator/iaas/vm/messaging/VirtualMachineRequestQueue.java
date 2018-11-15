/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.iaas.vm.messaging;

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

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineRequestQueue.class);
  private final BlockingQueue<UserCreateVirtualMachineRequest> pendingRequests;

  public VirtualMachineRequestQueue() {
    this.pendingRequests = new LinkedBlockingQueue<>();
  }

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
