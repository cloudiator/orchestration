package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;

public class CloudCredentialDomainRepository {

  private final CloudCredentialModelRepository cloudCredentialModelRepository;

  @Inject
  public CloudCredentialDomainRepository(
      CloudCredentialModelRepository cloudCredentialModelRepository) {
    this.cloudCredentialModelRepository = cloudCredentialModelRepository;
  }


  public void save(CloudCredential domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  CloudCredentialModel saveAndGet(CloudCredential domain) {
    checkNotNull(domain, "domain is null");
    final CloudCredentialModel model = createModel(domain);
    cloudCredentialModelRepository.save(model);
    return model;
  }

  void update(CloudCredential domain, CloudCredentialModel model) {
    updateModel(domain, model);
    cloudCredentialModelRepository.save(model);
  }

  private CloudCredentialModel createModel(CloudCredential domain) {
    return new CloudCredentialModel(domain.user(), domain.password());
  }


  private void updateModel(CloudCredential domain, CloudCredentialModel model) {
    checkState(domain.user().equals(model.getUser()), "updating user not permitted.");
    model.setPassword(domain.password());
  }
}
