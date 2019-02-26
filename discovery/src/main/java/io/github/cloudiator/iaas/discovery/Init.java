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
import com.google.inject.persist.PersistService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import io.github.cloudiator.persistance.CloudDomainRepository;
import java.util.Set;

/**
 * Created by daniel on 31.05.17.
 */
public class Init {

  private final PersistService persistService;
  private final ExecutionService executionService;
  private final Set<AbstractDiscoveryWorker> discoveryWorkerSet;
  private final DiscoveryListenerWorker discoveryListenerWorker;
  private final CloudRegistry cloudRegistry;
  private final CloudDomainRepository cloudDomainRepository;

  @Inject
  Init(PersistService persistService,
      ExecutionService executionService,
      Set<AbstractDiscoveryWorker> discoveryWorkerSet,
      DiscoveryListenerWorker discoveryListenerWorker,
      CloudRegistry cloudRegistry,
      CloudDomainRepository cloudDomainRepository) {
    this.persistService = persistService;
    this.executionService = executionService;
    this.discoveryWorkerSet = discoveryWorkerSet;
    this.discoveryListenerWorker = discoveryListenerWorker;
    this.cloudRegistry = cloudRegistry;
    this.cloudDomainRepository = cloudDomainRepository;

    run();
  }

  private void run() {
    startPersistService();
    runDiscoveryWorkers();
    runDiscoveryListenerWorker();
    restoreCloudRegistry();
  }

  private void startPersistService() {
    persistService.start();
  }

  private void runDiscoveryWorkers() {
    discoveryWorkerSet.forEach(
        executionService::schedule);
  }

  private void runDiscoveryListenerWorker() {
    executionService.execute(discoveryListenerWorker);
  }

  private void restoreCloudRegistry() {
    cloudDomainRepository.findAll().forEach(cloudRegistry::register);
  }


}
