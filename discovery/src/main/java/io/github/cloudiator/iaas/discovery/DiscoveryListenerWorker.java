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

package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class DiscoveryListenerWorker implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageDiscoveryListener.class);
  private final DiscoveryQueue discoveryQueue;
  private final Set<DiscoveryListener> discoveryListeners;

  @Inject
  public DiscoveryListenerWorker(DiscoveryQueue discoveryQueue,
      Set<DiscoveryListener> discoveryListeners) {
    this.discoveryQueue = discoveryQueue;
    this.discoveryListeners = discoveryListeners;
  }

  private Set<DiscoveryListener> interestedIn(Discovery discovery) {
    return discoveryListeners.stream().filter(
        discoveryListener -> discoveryListener.interestedIn().isAssignableFrom(discovery.getType()))
        .collect(Collectors.toSet());
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        Discovery discovery = discoveryQueue.take();
        interestedIn(discovery).forEach(
            discoveryListener -> discoveryListener.handle(discovery.discovery()));
      } catch (InterruptedException e) {
        LOGGER.warn(String.format("%s got interrupted.", this), e);
        Thread.currentThread().interrupt();
      }
    }
  }
}
