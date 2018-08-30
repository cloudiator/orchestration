package org.cloudiator.iaas.node.config;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.cloudiator.iaas.node.CompositeNodeCandidateIncarnation.CompositeNodeCandidateIncarnationFactory;
import org.cloudiator.iaas.node.Init;
import org.cloudiator.iaas.node.NodeCandidateIncarnation.NodeCandidateIncarnationFactory;
import org.cloudiator.iaas.node.VirtualMachineNodeIncarnation.VirtualMachineNodeIncarnationFactory;


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
  }
}
