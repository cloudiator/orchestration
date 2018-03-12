package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.CredentialsBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

public class CloudCredentialConverter implements
    OneWayConverter<CloudCredentialModel, CloudCredential> {

  @Nullable
  @Override
  public CloudCredential apply(@Nullable CloudCredentialModel cloudCredentialModel) {
    if (cloudCredentialModel == null) {
      return null;
    }

    return CredentialsBuilder.newBuilder()
        .user(cloudCredentialModel.getUser())
        .password(cloudCredentialModel.getPassword()).build();
  }
}
