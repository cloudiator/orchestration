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

package org.cloudiator.iaas.node;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeType;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.cloudiator.messages.Vm.DeleteVirtualMachineRequestMessage;
import org.cloudiator.messages.Vm.VirtualMachineDeletedResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.VirtualMachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineNodeDeletionStrategy implements NodeDeletionStrategy {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineNodeDeletionStrategy.class);
  private final VirtualMachineService virtualMachineService;

  @Inject
  public VirtualMachineNodeDeletionStrategy(
      VirtualMachineService virtualMachineService) {
    this.virtualMachineService = virtualMachineService;
  }

  @Override
  public boolean supportsNode(Node node) {
    if (node.type().equals(NodeType.VM)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteNode(Node node, String userId) {

    checkNotNull(node, "node is null");
    checkNotNull(userId, "userId is null");

    SettableFutureResponseCallback<VirtualMachineDeletedResponse, VirtualMachineDeletedResponse> future = SettableFutureResponseCallback
        .create();
    virtualMachineService.deleteVirtualMachineAsync(
        DeleteVirtualMachineRequestMessage.newBuilder().setVmId(node.id()).setUserId(userId)
            .build(), future);

    try {
      future.get();
      return true;
    } catch (InterruptedException e) {
      LOGGER.error(String.format("%s got interrupted while waiting for response.", this));
      return false;
    } catch (ExecutionException e) {
      LOGGER.error(String
          .format("Deletion of node %s failed, as virtual machine delete request failed with %s.",
              node, e.getCause().getMessage()), e);
      return false;
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
