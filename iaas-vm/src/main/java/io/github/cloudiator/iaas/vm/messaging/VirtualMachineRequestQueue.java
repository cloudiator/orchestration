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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 29.06.17.
 */
public class VirtualMachineRequestQueue {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineRequestQueue.class);
  private final BlockingQueue<VirtualMachineRequest> pendingRequests;

  public VirtualMachineRequestQueue() {
    this.pendingRequests = new LinkedBlockingQueue<>();
  }

  VirtualMachineRequest take() throws InterruptedException {
    return pendingRequests.take();
  }

  void add(VirtualMachineRequest virtualMachineRequest) {
    LOGGER.debug(String
        .format("New request %s was added to %s. Currently %s requests pending.",
            virtualMachineRequest, this,
            pendingRequests.size()));
    pendingRequests
        .add(virtualMachineRequest);
  }


}
