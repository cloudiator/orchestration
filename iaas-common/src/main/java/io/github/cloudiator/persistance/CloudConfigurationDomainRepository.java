package io.github.cloudiator.persistance;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;

class CloudConfigurationDomainRepository {

  private final ModelRepository<CloudConfigurationModel> cloudConfigurationModelModelRepository;

  @Inject
  CloudConfigurationDomainRepository(
      ModelRepository<CloudConfigurationModel> cloudConfigurationModelModelRepository) {
    this.cloudConfigurationModelModelRepository = cloudConfigurationModelModelRepository;
  }

  public void save(Configuration domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  CloudConfigurationModel saveAndGet(Configuration domain) {
    checkNotNull(domain, "domain is null");
    final CloudConfigurationModel model = createModel(domain);
    cloudConfigurationModelModelRepository.save(model);
    return model;
  }

  void update(Configuration domain, CloudConfigurationModel model) {
    updateModel(domain, model);
    cloudConfigurationModelModelRepository.save(model);
  }

  private CloudConfigurationModel createModel(Configuration domain) {

    final CloudConfigurationModel cloudConfigurationModel = new CloudConfigurationModel(
        domain.nodeGroup());
    setProperties(cloudConfigurationModel, domain);

    return cloudConfigurationModel;
  }

  private void setProperties(CloudConfigurationModel cloudConfigurationModel,
      Configuration domain) {
    domain.properties().getProperties().forEach((key, value) -> cloudConfigurationModel
        .addProperty(new PropertyModel(cloudConfigurationModel, key, value)));
  }

  private void updateModel(Configuration domain, CloudConfigurationModel model) {

    checkState(domain.nodeGroup().equals(model.getNodeGroup()),
        "Updating node group is not allowed");
    model.getProperties().clear();

    setProperties(model, domain);
  }
}
