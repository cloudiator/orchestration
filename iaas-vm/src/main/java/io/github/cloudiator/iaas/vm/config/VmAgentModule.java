package io.github.cloudiator.iaas.vm.config;

import static io.github.cloudiator.iaas.vm.Constants.VM_PARALLEL_STARTS;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import io.github.cloudiator.iaas.vm.messaging.VirtualMachineRequestQueue;
import io.github.cloudiator.iaas.vm.VmAgentContext;

public class VmAgentModule extends AbstractModule {

  private final VmAgentContext vmAgentContext;

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
    bindConstant().annotatedWith(Names.named(VM_PARALLEL_STARTS))
        .to(vmAgentContext.parallelVMStarts());
  }
}
