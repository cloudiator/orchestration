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

package io.github.cloudiator.iaas.discovery.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import io.github.cloudiator.iaas.discovery.AbstractDiscoveryWorker;
import io.github.cloudiator.iaas.discovery.DiscoveryListener;
import io.github.cloudiator.iaas.discovery.DiscoveryQueue;
import io.github.cloudiator.iaas.discovery.HardwareDiscoveryListener;
import io.github.cloudiator.iaas.discovery.HardwareDiscoveryWorker;
import io.github.cloudiator.iaas.discovery.ImageDiscoveryListener;
import io.github.cloudiator.iaas.discovery.ImageDiscoveryWorker;
import io.github.cloudiator.iaas.discovery.Init;
import io.github.cloudiator.iaas.discovery.LocationDiscoveryListener;
import io.github.cloudiator.iaas.discovery.LocationDiscoveryWorker;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandlerImpl;
import org.cloudiator.meta.cloudharmony.config.CloudHarmonyMetaModule;

/**
 * Created by daniel on 31.05.17.
 */
public class DiscoveryModule extends AbstractModule {

  @Override
  protected void configure() {
    final MultiCloudService multiCloudService = MultiCloudBuilder.newBuilder()
        .metaModule(new CloudHarmonyMetaModule()).build();
    bind(CloudRegistry.class).toInstance(multiCloudService.cloudRegistry());
    bind(ComputeService.class).toInstance(multiCloudService.computeService());
    bind(DiscoveryService.class).toInstance(multiCloudService.computeService().discoveryService());
    bind(ExecutionService.class).toInstance(
        new ScheduledThreadPoolExecutorExecutionService(
            new LoggingScheduledThreadPoolExecutor(10)));
    bind(Init.class).asEagerSingleton();
    bind(DiscoveryQueue.class).in(Singleton.class);
    bind(DiscoveryErrorHandler.class).to(DiscoveryErrorHandlerImpl.class);

    Multibinder<AbstractDiscoveryWorker> discoveryWorkerBinder = Multibinder
        .newSetBinder(binder(), AbstractDiscoveryWorker.class);
    discoveryWorkerBinder.addBinding().to(ImageDiscoveryWorker.class);
    discoveryWorkerBinder.addBinding().to(LocationDiscoveryWorker.class);
    discoveryWorkerBinder.addBinding().to(HardwareDiscoveryWorker.class);

    Multibinder<DiscoveryListener> discoveryListenerBinder = Multibinder
        .newSetBinder(binder(), DiscoveryListener.class);
    discoveryListenerBinder.addBinding().to(ImageDiscoveryListener.class);
    discoveryListenerBinder.addBinding().to(LocationDiscoveryListener.class);
    discoveryListenerBinder.addBinding().to(HardwareDiscoveryListener.class);

  }
}
