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

package org.cloudiator.iaas.node.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.cloudiator.iaas.node.config.NodeAgentConstants.NODE_EXECUTION_SERVICE_NAME;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import de.uniulm.omi.cloudiator.util.execution.LoggingThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.cloudiator.iaas.node.ByonNodeDeletionStrategy;
import org.cloudiator.iaas.node.ByonNodeSchedulingStrategy;
import org.cloudiator.iaas.node.CompositeNodeDeletionStrategy;
import org.cloudiator.iaas.node.CompositeNodeSchedulingStrategy;
import org.cloudiator.iaas.node.FaasNodeDeletionStrategy;
import org.cloudiator.iaas.node.FaasNodeSchedulingStrategy;
import org.cloudiator.iaas.node.Init;
import org.cloudiator.iaas.node.NodeDeletionStrategy;
import org.cloudiator.iaas.node.NodeSchedulingStrategy;
import org.cloudiator.iaas.node.VirtualMachineNodeDeletionStrategy;
import org.cloudiator.iaas.node.VirtualMachineNodeSchedulingStrategy;
import org.cloudiator.iaas.node.messaging.CreateNodeRequestWorkerFactory;
import org.cloudiator.iaas.node.messaging.DeleteNodeRequestWorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by daniel on 31.05.17.
 */
public class NodeModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeModule.class);
  private final NodeAgentContext nodeAgentContext;

  public NodeModule(NodeAgentContext nodeAgentContext) {
    checkNotNull(nodeAgentContext, "nodeAgentContext is null");
    this.nodeAgentContext = nodeAgentContext;
  }

  @Override
  protected void configure() {

    bind(Init.class).asEagerSingleton();

    install(new FactoryModuleBuilder().build(CreateNodeRequestWorkerFactory.class));
    install(new FactoryModuleBuilder().build(DeleteNodeRequestWorkerFactory.class));

    final int parallelNodes = nodeAgentContext.parallelNodes();

    final LoggingThreadPoolExecutor nodeExecutor = new LoggingThreadPoolExecutor(
        parallelNodes, parallelNodes, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());

    bind(ExecutorService.class).annotatedWith(Names.named(NODE_EXECUTION_SERVICE_NAME)).toInstance(
        nodeExecutor
    );

    LOGGER.info(String
        .format("Allowing parallel execution of %s node requests", parallelNodes));

    LOGGER.info(String.format("Registering shutdown hook for node execution service %s",
        nodeExecutor));
    MoreExecutors.addDelayedShutdownHook(nodeExecutor, 5, TimeUnit.MINUTES);

    Multibinder<NodeSchedulingStrategy> multibinder = Multibinder
        .newSetBinder(binder(), NodeSchedulingStrategy.class);
    multibinder.addBinding().to(VirtualMachineNodeSchedulingStrategy.class);
    multibinder.addBinding().to(FaasNodeSchedulingStrategy.class);
    multibinder.addBinding().to(ByonNodeSchedulingStrategy.class);
    bind(NodeSchedulingStrategy.class).to(CompositeNodeSchedulingStrategy.class);

    Multibinder<NodeDeletionStrategy> nodeDeletionStrategyMultibinder = Multibinder
        .newSetBinder(binder(), NodeDeletionStrategy.class);
    nodeDeletionStrategyMultibinder.addBinding().to(VirtualMachineNodeDeletionStrategy.class);
    nodeDeletionStrategyMultibinder.addBinding().to(FaasNodeDeletionStrategy.class);
    nodeDeletionStrategyMultibinder.addBinding().to(ByonNodeDeletionStrategy.class);

    bind(NodeDeletionStrategy.class).to(CompositeNodeDeletionStrategy.class);
  }
}
