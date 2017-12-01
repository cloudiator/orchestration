package io.github.cloudiator.iaas.common.messaging.converters;

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

    switch (loginCredential.getCredentialCase()) {
      case PASSWORD:
        loginCredentialBuilder.password(loginCredential.getPassword());
        break;
      case PRIVATEKEY:
        loginCredentialBuilder.privateKey(loginCredential.getPrivateKey());
      case CREDENTIAL_NOT_SET:
        break;
      default:
        throw new AssertionError("Unknown credential case " + loginCredential.getCredentialCase());
    }

    return loginCredentialBuilder.build();
  }
}
