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

import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import de.uniulm.omi.cloudiator.sword.domain.Pricing;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.sword.multicloud.pricing.PricingSupplierFactory;
import de.uniulm.omi.cloudiator.sword.multicloud.pricing.aws.AWSPricingSupplier;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import de.uniulm.omi.cloudiator.sword.service.PricingService;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import io.github.cloudiator.iaas.discovery.*;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandlerImpl;
import org.cloudiator.meta.cloudharmony.config.CloudHarmonyMetaModule;

import java.util.Set;

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
    bind(PricingService.class).toInstance(multiCloudService.pricingService());

    Multibinder<AbstractDiscoveryWorker> discoveryWorkerBinder = Multibinder
        .newSetBinder(binder(), AbstractDiscoveryWorker.class);
    discoveryWorkerBinder.addBinding().to(ImageDiscoveryWorker.class);
    discoveryWorkerBinder.addBinding().to(LocationDiscoveryWorker.class);
    discoveryWorkerBinder.addBinding().to(HardwareDiscoveryWorker.class);
    discoveryWorkerBinder.addBinding().to(PricingDiscoveryWorker.class);

    Multibinder<DiscoveryListener> discoveryListenerBinder = Multibinder
        .newSetBinder(binder(), DiscoveryListener.class);
    discoveryListenerBinder.addBinding().to(ImageDiscoveryListener.class);
    discoveryListenerBinder.addBinding().to(LocationDiscoveryListener.class);
    discoveryListenerBinder.addBinding().to(HardwareDiscoveryListener.class);
    discoveryListenerBinder.addBinding().to(PricingDiscoveryListener.class);

    install(new FactoryModuleBuilder()
            .implement(new TypeLiteral<Supplier<Set<Pricing>>>(){}, Names.named("aws"), AWSPricingSupplier.class)
            .build(PricingSupplierFactory.class));
  }
}
