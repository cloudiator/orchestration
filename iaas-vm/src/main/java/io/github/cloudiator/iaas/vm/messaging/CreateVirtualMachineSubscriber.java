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

          //todo: validate request

          virtualMachineRequestQueue.add(VirtualMachineRequest.of(requestId, request));
        });
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
