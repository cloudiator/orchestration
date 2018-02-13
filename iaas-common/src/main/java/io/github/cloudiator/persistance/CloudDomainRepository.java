package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by daniel on 09.06.17.
 */
public class CloudDomainRepository {

  private final static CloudConverter CLOUD_CONVERTER = new CloudConverter();
  private final ApiDomainRepository apiDomainRepository;
  private final CloudCredentialDomainRepository cloudCredentialDomainRepository;
  private final CloudModelRepository cloudModelRepository;
  private final TenantModelRepository tenantModelRepository;
  private final CloudRegistry cloudRegistry;
  private final CloudConfigurationDomainRepository cloudConfigurationDomainRepository;

  @Inject public CloudDomainRepository(
      ApiDomainRepository apiDomainRepository,
      CloudCredentialDomainRepository cloudCredentialDomainRepository,
      CloudModelRepository cloudModelRepository,
      TenantModelRepository tenantModelRepository,
      CloudRegistry cloudRegistry,
      CloudConfigurationDomainRepository cloudConfigurationDomainRepository) {
    this.apiDomainRepository = apiDomainRepository;
    this.cloudCredentialDomainRepository = cloudCredentialDomainRepository;
    this.cloudModelRepository = cloudModelRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.cloudRegistry = cloudRegistry;
    this.cloudConfigurationDomainRepository = cloudConfigurationDomainRepository;
  }

  public void save(Cloud domain, String userId) {
    checkNotNull(domain, "domain is null");
    checkNotNull(userId, "userId is null");

    final CloudModel byCloudId = cloudModelRepository.getByCloudId(domain.id());

    if (byCloudId == null) {
      final CloudModel cloudModel = createModel(domain, userId);
      cloudModelRepository.save(cloudModel);
      onAfterAdd(domain);
    } else {
      onBeforeUpdate(domain);
      final CloudModel cloudModel = updateModel(domain, byCloudId, userId);
      cloudModelRepository.save(cloudModel);
      onAfterUpdate(domain);
    }
  }

  private void onAfterAdd(Cloud cloud) {
    checkState(!cloudRegistry.isRegistered(cloud), "cloud was already registered.");
    cloudRegistry.register(cloud);
  }

  private void onBeforeUpdate(Cloud cloud) {
    checkState(cloudRegistry.isRegistered(cloud), "cloud was never registered.");
    cloudRegistry.unregister(cloud.id());
  }

  private void onAfterUpdate(Cloud cloud) {
    checkState(!cloudRegistry.isRegistered(cloud),
        "cloud is already registered, expected unregistered before update");
    cloudRegistry.register(cloud);
  }

  private TenantModel createIfNotExists(String userId) {
    TenantModel tenantModel = tenantModelRepository.findByUserId(userId);
    if (tenantModel == null) {
      tenantModel = new TenantModel(userId);
      tenantModelRepository.save(tenantModel);
    }
    return tenantModel;
  }

  private CloudModel createModel(Cloud domain, String userId) {

    final TenantModel tenantModel = createIfNotExists(userId);

    //create or get Api
    final ApiModel apiModel = apiDomainRepository.saveOrGet(domain.api());

    //create credential
    final CloudCredentialModel cloudCredentialModel = cloudCredentialDomainRepository
        .saveAndGet(domain.credential());

    //create configuration
    final CloudConfigurationModel cloudConfigurationModel = cloudConfigurationDomainRepository
        .saveAndGet(domain.configuration());

    CloudModel cloudModel = new CloudModel(domain.id(), tenantModel, apiModel,
        domain.endpoint().orElse(null), cloudCredentialModel, cloudConfigurationModel,
        domain.cloudType());
    cloudModelRepository.save(cloudModel);

    return cloudModel;
  }

  private CloudModel updateModel(Cloud domain, CloudModel model, String userId) {

    checkState(model.getTenantModel().getUserId().equals(userId), "updating userId not allowed");

    if (model.getEndpoint() == null) {
      checkState(!domain.endpoint().isPresent(), "updating endpoint is not allowed");
    } else {
      checkState(
          domain.endpoint().isPresent() && domain.endpoint().get().equals(model.getEndpoint()),
          "updating endpoint is not allowed");
    }

    checkState(domain.api().providerName().equals(model.getApiModel().getProviderName()),
        "updating api not allowed");

    model.setCloudType(domain.cloudType());

    cloudCredentialDomainRepository.update(domain.credential(), model.getCloudCredential());
    cloudConfigurationDomainRepository
        .update(domain.configuration(), model.getCloudConfiguration());

    return model;
  }

  public Collection<Cloud> findAll(String userId) {
    return cloudModelRepository.getByTenant(userId).stream().map(CLOUD_CONVERTER)
        .collect(Collectors.toList());
  }

  public Collection<Cloud> findAll() {
    return cloudModelRepository.findAll().stream().map(CLOUD_CONVERTER)
        .collect(Collectors.toList());
  }

  public Cloud findByUserAndId(String user, String cloudId) {
    return CLOUD_CONVERTER.apply(cloudModelRepository.getByTenantAndId(user, cloudId));
  }

  public Cloud findById(String cloudId) {
    checkNotNull(cloudId, "cloudId is null");
    return CLOUD_CONVERTER.apply(findModelById(cloudId));
  }

  CloudModel findModelById(String id) {
    checkNotNull(id, "id is null");
    return cloudModelRepository.getByCloudId(id);
  }
}
