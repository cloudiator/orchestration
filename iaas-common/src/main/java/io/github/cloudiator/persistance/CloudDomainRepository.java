package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import io.github.cloudiator.domain.ExtendedCloud;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by daniel on 09.06.17.
 */
public class CloudDomainRepository {

  private final static CloudModelConverter CLOUD_CONVERTER = new CloudModelConverter();
  private final ApiDomainRepository apiDomainRepository;
  private final CloudCredentialDomainRepository cloudCredentialDomainRepository;
  private final CloudModelRepository cloudModelRepository;
  private final TenantModelRepository tenantModelRepository;
  private final CloudConfigurationDomainRepository cloudConfigurationDomainRepository;

  @Inject
  public CloudDomainRepository(
      ApiDomainRepository apiDomainRepository,
      CloudCredentialDomainRepository cloudCredentialDomainRepository,
      CloudModelRepository cloudModelRepository,
      TenantModelRepository tenantModelRepository,
      CloudConfigurationDomainRepository cloudConfigurationDomainRepository) {
    this.apiDomainRepository = apiDomainRepository;
    this.cloudCredentialDomainRepository = cloudCredentialDomainRepository;
    this.cloudModelRepository = cloudModelRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.cloudConfigurationDomainRepository = cloudConfigurationDomainRepository;
  }

  public void save(ExtendedCloud domain) {
    checkNotNull(domain, "domain is null");

    final CloudModel byCloudId = cloudModelRepository.getByCloudId(domain.id());

    if (byCloudId == null) {
      final CloudModel cloudModel = createModel(domain);
      cloudModelRepository.save(cloudModel);
    } else {
      final CloudModel cloudModel = updateModel(domain, byCloudId);
      cloudModelRepository.save(cloudModel);
    }
  }

  public void delete(String id, String userId) {
    final ExtendedCloud byUserAndId = findByUserAndId(userId, id);
    if (byUserAndId == null) {
      throw new IllegalStateException(
          String.format("Cloud with id %s does not exist. Can not be deleted.", id));
    }
    cloudModelRepository.delete(cloudModelRepository.getByCloudId(id));
  }


  private TenantModel createIfNotExists(String userId) {
    TenantModel tenantModel = tenantModelRepository.findByUserId(userId);
    if (tenantModel == null) {
      tenantModel = new TenantModel(userId);
      tenantModelRepository.save(tenantModel);
    }
    return tenantModel;
  }

  private CloudModel createModel(ExtendedCloud domain) {

    final TenantModel tenantModel = createIfNotExists(domain.userId());

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
        domain.cloudType(), domain.state(), domain.diagnostic().orElse(null));
    cloudModelRepository.save(cloudModel);

    return cloudModel;
  }

  private CloudModel updateModel(ExtendedCloud domain, CloudModel model) {

    checkState(model.getTenantModel().getUserId().equals(domain.userId()),
        "updating userId not allowed");

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
    model.setCloudState(domain.state());

    cloudCredentialDomainRepository.update(domain.credential(), model.getCloudCredential());
    cloudConfigurationDomainRepository
        .update(domain.configuration(), model.getCloudConfiguration());

    return model;
  }

  public Collection<ExtendedCloud> findAll(String userId) {
    return cloudModelRepository.getByTenant(userId).stream().map(CLOUD_CONVERTER)
        .collect(Collectors.toList());
  }

  public Collection<ExtendedCloud> findAll() {
    return cloudModelRepository.findAll().stream().map(CLOUD_CONVERTER)
        .collect(Collectors.toList());
  }

  public ExtendedCloud findByUserAndId(String user, String cloudId) {
    return CLOUD_CONVERTER.apply(cloudModelRepository.getByTenantAndId(user, cloudId));
  }

  public ExtendedCloud findById(String cloudId) {
    checkNotNull(cloudId, "cloudId is null");
    return CLOUD_CONVERTER.apply(findModelById(cloudId));
  }

  CloudModel findModelById(String id) {
    checkNotNull(id, "id is null");
    return cloudModelRepository.getByCloudId(id);
  }
}
