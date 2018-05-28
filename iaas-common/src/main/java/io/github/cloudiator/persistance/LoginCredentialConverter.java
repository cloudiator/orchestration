package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

public class LoginCredentialConverter implements
    OneWayConverter<LoginCredentialModel, LoginCredential> {

  @Nullable
  @Override
  public LoginCredential apply(@Nullable LoginCredentialModel loginCredentialModel) {
    if (loginCredentialModel == null) {
      return null;
    }
  }
}
