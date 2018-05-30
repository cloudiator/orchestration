package io.github.cloudiator.iaas.vm.config;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.messaging.CloudMessageRepository;
import io.github.cloudiator.persistance.TenantDomainRepository;

/**
 * Created by daniel on 31.05.17.
 */
class Init {

  private final PersistService persistService;
  private final CloudMessageRepository cloudMessageRepository;
  private final TenantDomainRepository tenantDomainRepository;
  private final CloudRegistry cloudRegistry;

  @Inject
  Init(PersistService persistService,
      CloudMessageRepository cloudMessageRepository,
      TenantDomainRepository tenantDomainRepository,
      CloudRegistry cloudRegistry) {
    this.persistService = persistService;
    this.cloudMessageRepository = cloudMessageRepository;
    this.tenantDomainRepository = tenantDomainRepository;
    this.cloudRegistry = cloudRegistry;
    run();
  }

  private void run() {
    startPersistService();
    restoreCloudRegistry();
  }

  private void startPersistService() {
    persistService.start();
  }

  private void restoreCloudRegistry() {
    for (String tenant : tenantDomainRepository.tenants()) {
      cloudMessageRepository.getAll(tenant).forEach(cloud -> {
        if (!cloudRegistry.isRegistered(cloud)) {
          cloudRegistry.register(cloud);
        }
      });
    }
  }

}
