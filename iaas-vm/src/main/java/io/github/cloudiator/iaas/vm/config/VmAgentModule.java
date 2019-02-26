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

package io.github.cloudiator.iaas.vm.config;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import de.uniulm.omi.cloudiator.util.execution.LoggingThreadPoolExecutor;
import io.github.cloudiator.iaas.vm.VirtualMachineAgent;
import io.github.cloudiator.iaas.vm.VmAgentContext;
import io.github.cloudiator.iaas.vm.messaging.VirtualMachineRequestQueue;
import io.github.cloudiator.iaas.vm.messaging.VirtualMachineRequestWorkerFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmAgentModule extends AbstractModule {

  private final VmAgentContext vmAgentContext;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(VirtualMachineAgent.class);

  public VmAgentModule(VmAgentContext vmAgentContext) {
    this.vmAgentContext = vmAgentContext;
  }

  @Override
  protected void configure() {
    final MultiCloudService multiCloudService = MultiCloudBuilder.newBuilder()
        .build();
    bind(CloudRegistry.class).toInstance(multiCloudService.cloudRegistry());
    bind(ComputeService.class).toInstance(multiCloudService.computeService());
    bind(DiscoveryService.class).toInstance(multiCloudService.computeService().discoveryService());
    bind(Init.class).asEagerSingleton();
    bind(VirtualMachineRequestQueue.class).in(Singleton.class);

    install(new FactoryModuleBuilder().build(VirtualMachineRequestWorkerFactory.class));

    final int parallelVMStarts = vmAgentContext.parallelVMStarts();

    final LoggingThreadPoolExecutor vmExecutor = new LoggingThreadPoolExecutor(
        parallelVMStarts, parallelVMStarts, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());

    bind(ExecutorService.class).annotatedWith(Names.named("VM_WORKERS")).toInstance(
        vmExecutor
    );

    LOGGER.info(String
        .format("Allowing parallel execution of %s virtual machine requests", parallelVMStarts));

    LOGGER.info(String.format("Registering shutdown hook for virtual machine execution service %s",
        vmExecutor));
    MoreExecutors.addDelayedShutdownHook(vmExecutor, 5, TimeUnit.MINUTES);

  }
}
