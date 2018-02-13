package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.CredentialsBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities.Credential;

/**
 * Created by daniel on 31.05.17.
 */
class CredentialMessageToCredential implements
    TwoWayConverter<Credential, CloudCredential> {

  @Override
  public CloudCredential apply(Credential credential) {
    return CredentialsBuilder.newBuilder().user(credential.getUser())
        .password(credential.getSecret()).build();
  }

  @Override
  public Credential applyBack(CloudCredential cloudCredential) {
    return Credential.newBuilder().setSecret(cloudCredential.password())
        .setUser(cloudCredential.user()).build();
  }
}
