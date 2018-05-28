package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class LoginCredentialConverter implements
    OneWayConverter<LoginCredentialModel, LoginCredential> {

  @Nullable
  @Override
  public LoginCredential apply(@Nullable LoginCredentialModel loginCredentialModel) {
    if (loginCredentialModel == null) {
      return null;
    }

    return LoginCredentialBuilder.newBuilder().password(loginCredentialModel.getPassword())
        .privateKey(loginCredentialModel.getPrivateKey())
        .username(loginCredentialModel.getUsername()).build();
  }
}
