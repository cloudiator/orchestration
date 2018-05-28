package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;

public class LoginCredentialDomainRepository {

  private final LoginCredentialModelRepository loginCredentialModelRepository;

  @Inject
  public LoginCredentialDomainRepository(
      LoginCredentialModelRepository loginCredentialModelRepository) {
    this.loginCredentialModelRepository = loginCredentialModelRepository;
  }

  LoginCredentialModel saveAndGet(LoginCredential domain) {
    final LoginCredentialModel model = createModel(domain);
    loginCredentialModelRepository.save(model);
    return model;
  }

  private LoginCredentialModel createModel(LoginCredential domain) {
    return new LoginCredentialModel(domain.username().orElse(null), domain.password().orElse(null),
        domain.privateKey().orElse(null));
  }


}
