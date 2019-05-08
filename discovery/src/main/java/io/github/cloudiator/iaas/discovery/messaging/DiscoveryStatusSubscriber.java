/*
 * Copyright (c) 2014-2019 University of Ulm
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

package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.iaas.discovery.AbstractDiscoveryWorker;
import java.util.Map;
import org.cloudiator.messages.Discovery.DiscoverStatusResponse;
import org.cloudiator.messages.Discovery.DiscoveryStatusRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.IaasEntities.DiscoveryStatus;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;

public class DiscoveryStatusSubscriber implements Runnable {

  private final MessageInterface messageInterface;

  @Inject
  public DiscoveryStatusSubscriber(MessageInterface messageInterface) {
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    Subscription subscription = messageInterface
        .subscribe(DiscoveryStatusRequest.class, DiscoveryStatusRequest.parser(),
            (requestId, discoveryStatusRequest) -> {

              try {
                Map<String, Integer> status = AbstractDiscoveryWorker.DISCOVERY_STATUS;

                final DiscoverStatusResponse discoverStatusResponse = DiscoverStatusResponse
                    .newBuilder()
                    .setDiscoveryStatus(DiscoveryStatus.newBuilder().putAllStatus(status).build())
                    .build();

                messageInterface.reply(requestId, discoverStatusResponse);

              } catch (Exception e) {
                messageInterface.reply(DiscoverStatusResponse.class, requestId,
                    Error.newBuilder().setCode(500)
                        .setMessage("Unexpected error: " + e.getMessage()).build());
              }

            });
  }
}
