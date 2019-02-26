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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.cloudiator.iaas.node.CompositeNodeDeletionStrategy;
import org.cloudiator.iaas.node.CompositeNodeSchedulingStrategy;
import org.cloudiator.iaas.node.FaasNodeDeletionStrategy;
import org.cloudiator.iaas.node.FaasNodeSchedulingStrategy;
import org.cloudiator.iaas.node.Init;
import org.cloudiator.iaas.node.NodeDeletionStrategy;
import org.cloudiator.iaas.node.NodeSchedulingStrategy;
import org.cloudiator.iaas.node.VirtualMachineNodeDeletionStrategy;
import org.cloudiator.iaas.node.VirtualMachineNodeSchedulingStrategy;


/**
 * Created by daniel on 31.05.17.
 */
public class NodeModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(Init.class).asEagerSingleton();
    bind(NodeSchedulingStrategy.class).to(CompositeNodeSchedulingStrategy.class);

    Multibinder<NodeSchedulingStrategy> multibinder = Multibinder
        .newSetBinder(binder(), NodeSchedulingStrategy.class);
    multibinder.addBinding().to(VirtualMachineNodeSchedulingStrategy.class);
    multibinder.addBinding().to(FaasNodeSchedulingStrategy.class);

    Multibinder<NodeDeletionStrategy> nodeDeletionStrategyMultibinder = Multibinder
        .newSetBinder(binder(), NodeDeletionStrategy.class);
    nodeDeletionStrategyMultibinder.addBinding().to(VirtualMachineNodeDeletionStrategy.class);
    nodeDeletionStrategyMultibinder.addBinding().to(FaasNodeDeletionStrategy.class);

    bind(NodeDeletionStrategy.class).to(CompositeNodeDeletionStrategy.class);
  }
}
