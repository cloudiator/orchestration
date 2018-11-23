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
