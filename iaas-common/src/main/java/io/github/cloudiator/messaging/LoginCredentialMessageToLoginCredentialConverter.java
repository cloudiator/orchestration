package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.LoginCredential.Builder;

public class LoginCredentialMessageToLoginCredentialConverter implements
    TwoWayConverter<IaasEntities.LoginCredential, LoginCredential> {

  @Override
  public IaasEntities.LoginCredential applyBack(LoginCredential loginCredential) {
    Builder builder = IaasEntities.LoginCredential.newBuilder();
    if (loginCredential.password().isPresent()) {
      builder.setPassword(loginCredential.password().get());
    }
    if (loginCredential.privateKey().isPresent()) {
      builder.setPrivateKey(loginCredential.privateKey().get());
    }
    if (loginCredential.username().isPresent()) {
      builder.setUsername(loginCredential.username().get());
    }
    return builder.build();
  }

  @Override
  public LoginCredential apply(IaasEntities.LoginCredential loginCredential) {

    LoginCredentialBuilder loginCredentialBuilder = LoginCredentialBuilder.newBuilder();

    if (loginCredential.getUsername() != null && !loginCredential.getUsername().isEmpty()) {
      loginCredentialBuilder.username(loginCredential.getUsername());
    }

    if (loginCredential.getPassword() != null && !loginCredential.getPassword().isEmpty()) {
      loginCredentialBuilder.password(loginCredential.getPassword());
    }

    if (loginCredential.getPrivateKey() != null && !loginCredential.getPrivateKey().isEmpty()) {
      loginCredentialBuilder.privateKey(loginCredential.getPrivateKey());
    }

    return loginCredentialBuilder.build();
  }
}
