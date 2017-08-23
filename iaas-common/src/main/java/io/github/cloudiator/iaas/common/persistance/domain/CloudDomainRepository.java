package io.github.cloudiator.iaas.common.persistance.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.iaas.common.persistance.domain.converters.CloudConverter;
import io.github.cloudiator.iaas.common.persistance.entities.ApiModel;
import io.github.cloudiator.iaas.common.persistance.entities.CloudConfigurationModel;
import io.github.cloudiator.iaas.common.persistance.entities.CloudCredentialModel;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.entities.Tenant;
import io.github.cloudiator.iaas.common.persistance.repositories.ApiModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudConfigurationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudCredentialModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by daniel on 09.06.17.
 */
public class CloudDomainRepository {

  private final CloudModelRepository cloudModelRepository;
  private final CloudRegistry cloudRegistry;
  private final ApiModelRepository apiModelRepository;
  private final CloudCredentialModelRepository cloudCredentialModelRepository;
  private final TenantModelRepository tenantModelRepository;
  private final CloudConverter cloudConverter = new CloudConverter();
  private final CloudConfigurationModelRepository cloudConfigurationModelRepository;

  @Inject
  public CloudDomainRepository(
      CloudModelRepository cloudModelRepository,
      CloudRegistry cloudRegistry,
      ApiModelRepository apiModelRepository,
      CloudCredentialModelRepository cloudCredentialModelRepository,
      TenantModelRepository tenantModelRepository,
      CloudConfigurationModelRepository cloudConfigurationModelRepository) {
    this.cloudModelRepository = cloudModelRepository;
    this.cloudRegistry = cloudRegistry;
    this.apiModelRepository = apiModelRepository;
    this.cloudCredentialModelRepository = cloudCredentialModelRepository;

    this.tenantModelRepository = tenantModelRepository;
    this.cloudConfigurationModelRepository = cloudConfigurationModelRepository;
  }

  public Cloud findById(String id) {
    checkNotNull(id, "id is null");
    return cloudConverter.apply(cloudModelRepository.getByCloudId(id));
  }

  public void add(Cloud cloud, String userId) {

    checkNotNull(cloud, "cloud is null");

    if (findById(cloud.id()) != null) {
      throw new IllegalStateException(
          String.format("A cloud with the id %s does already exist.", cloud.id()));
    }

    //get tenant or create it
    Tenant tenant = tenantModelRepository.findByUserId(userId);
    if (tenant == null) {
      tenant = new Tenant(userId);
      tenantModelRepository.save(tenant);
    }

    //check if api exists, if not create it.
    ApiModel apiModel = apiModelRepository.findByProviderName(cloud.api().providerName());
    if (apiModel == null) {
      apiModel = new ApiModel(cloud.api().providerName());
      apiModelRepository.save(apiModel);
    }

    //create credential
    CloudCredentialModel cloudCredentialModel = new CloudCredentialModel(cloud.credential().user(),
        cloud.credential().password());
    cloudCredentialModelRepository.save(cloudCredentialModel);

    //create configuration
    CloudConfigurationModel cloudConfigurationModel = new CloudConfigurationModel(
        cloud.configuration().nodeGroup());
    for (Map.Entry<String, String> entry : cloud.configuration().properties().getProperties()
        .entrySet()) {
      cloudConfigurationModel.addProperty(entry.getKey(), entry.getValue());
    }
    cloudConfigurationModelRepository.save(cloudConfigurationModel);

    //create cloud model
    CloudModel cloudModel = new CloudModel(cloud.id(), tenant, apiModel,
        cloud.endpoint().orElse(null), cloudCredentialModel, cloudConfigurationModel,
        cloud.cloudType());
    cloudModelRepository.save(cloudModel);

    cloudRegistry.register(cloud);
  }

  public void delete(Cloud cloud) {
    checkNotNull(cloud, "cloud is null");
    cloudModelRepository.delete(cloudModelRepository.getByCloudId(cloud.id()));
    cloudRegistry.unregister(cloud);
  }

  public List<Cloud> findAll() {
    return cloudModelRepository.findAll().stream().map(cloudConverter).collect(Collectors.toList());
  }

  public List<Cloud> findByUser(String userId) {
    return cloudModelRepository.getByTenant(userId).stream().map(cloudConverter)
        .collect(Collectors.toList());
  }
}
