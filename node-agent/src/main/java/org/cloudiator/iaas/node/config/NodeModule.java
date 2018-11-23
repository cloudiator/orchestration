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
import org.cloudiator.iaas.node.*;
import org.cloudiator.iaas.node.CompositeNodeCandidateIncarnationStrategy.CompositeNodeCandidateIncarnationFactory;
import org.cloudiator.iaas.node.NodeCandidateIncarnationStrategy.NodeCandidateIncarnationFactory;
import org.cloudiator.iaas.node.VirtualMachineNodeIncarnationStrategy.VirtualMachineNodeIncarnationFactory;


/**
 * Created by daniel on 31.05.17.
 */
public class NodeModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(Init.class).asEagerSingleton();
    bind(NodeCandidateIncarnationFactory.class).to(CompositeNodeCandidateIncarnationFactory.class);

    Multibinder<NodeCandidateIncarnationFactory> multibinder = Multibinder
        .newSetBinder(binder(), NodeCandidateIncarnationFactory.class);
    multibinder.addBinding().to(VirtualMachineNodeIncarnationFactory.class);
    multibinder.addBinding().to(FaasNodeIncarnationStrategy.FaasNodeIncarnationFactory.class);

    Multibinder<NodeDeletionStrategy> nodeDeletionStrategyMultibinder = Multibinder
        .newSetBinder(binder(), NodeDeletionStrategy.class);
    nodeDeletionStrategyMultibinder.addBinding().to(VirtualMachineNodeDeletionStrategy.class);
    nodeDeletionStrategyMultibinder.addBinding().to(FaasNodeDeletionStrategy.class);

    bind(NodeDeletionStrategy.class).to(CompositeNodeDeletionStrategy.class);
  }
}
