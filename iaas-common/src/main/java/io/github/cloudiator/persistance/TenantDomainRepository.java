package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class TenantDomainRepository {

  private final TenantModelRepository tenantModelRepository;

  @Inject
  public TenantDomainRepository(
      TenantModelRepository tenantModelRepository) {
    this.tenantModelRepository = tenantModelRepository;
  }

  public List<String> tenants() {
    return tenantModelRepository.findAll().stream().map(TenantModel::getUserId)
        .collect(Collectors.toList());
  }


}
