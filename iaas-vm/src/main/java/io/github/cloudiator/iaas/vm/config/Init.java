package io.github.cloudiator.iaas.vm.config;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.messaging.CloudMessageRepository;
import io.github.cloudiator.persistance.TenantDomainRepository;
import java.util.stream.Collectors;
import org.cloudiator.messages.entities.User.TenantQueryRequest;
import org.cloudiator.messages.entities.User.TenantQueryResponse;
import org.cloudiator.messages.entities.UserEntities.Tenant;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.UserService;

/**
 * Created by daniel on 31.05.17.
 */
class Init {

  private final PersistService persistService;
  private final CloudMessageRepository cloudMessageRepository;
  private final TenantDomainRepository tenantDomainRepository;
  private final CloudRegistry cloudRegistry;
  private final UserService userService;

  @Inject
  Init(PersistService persistService,
      CloudMessageRepository cloudMessageRepository,
      TenantDomainRepository tenantDomainRepository,
      CloudRegistry cloudRegistry, UserService userService) {
    this.persistService = persistService;
    this.cloudMessageRepository = cloudMessageRepository;
    this.tenantDomainRepository = tenantDomainRepository;
    this.cloudRegistry = cloudRegistry;
    this.userService = userService;
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

    try {
      final TenantQueryResponse tenants = userService
          .getTenants(TenantQueryRequest.newBuilder().build());

      for (String tenant : tenants.getTenantList().stream().map(Tenant::getTenant)
          .collect(
              Collectors
                  .toSet())) {
        for (Cloud cloud : cloudMessageRepository.getAll(tenant)) {
          if (!cloudRegistry.isRegistered(cloud)) {
            cloudRegistry.register(cloud);
          }
        }
      }


    } catch (ResponseException e) {
      throw new IllegalStateException("Could not restore initial state of cloud registry");
    }
  }

}
