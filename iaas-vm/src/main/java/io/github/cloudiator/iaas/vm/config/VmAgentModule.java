package io.github.cloudiator.iaas.vm.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import io.github.cloudiator.iaas.vm.VirtualMachineRequestQueue;

public class VmAgentModule extends AbstractModule {

  @Override
  protected void configure() {
    final MultiCloudService multiCloudService = MultiCloudBuilder.newBuilder()
        .build();
    bind(CloudRegistry.class).toInstance(multiCloudService.cloudRegistry());
    bind(ComputeService.class).toInstance(multiCloudService.computeService());
    bind(DiscoveryService.class).toInstance(multiCloudService.computeService().discoveryService());
    bind(Init.class).asEagerSingleton();
    bind(VirtualMachineRequestQueue.class).in(Singleton.class);
  }
}
