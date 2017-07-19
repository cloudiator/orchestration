package io.github.cloudiator.iaas.common.messaging;

import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.LoginCredential.CredentialCase;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class LoginCredentialMessageToLoginCredential implements TwoWayConverter<IaasEntities.LoginCredential, LoginCredential> {

  KeyPairMessageToKeyPair keyPairMessageToKeyPair = new KeyPairMessageToKeyPair();


  @Override
  public IaasEntities.LoginCredential applyBack(LoginCredential loginCredential) {

    if(loginCredential.privateKey().isPresent()){
      //TODO: add keypair
      return IaasEntities.LoginCredential.newBuilder().setUsername(loginCredential.username().get()).build();
    }else{

    }

    return null;
  }

  @Override
  public LoginCredential apply(IaasEntities.LoginCredential loginCredential) {
    if(loginCredential.getCredentialCase().equals(CredentialCase.KEYPAIR)){
      return LoginCredentialBuilder.newBuilder().username(loginCredential.getUsername()).privateKey(loginCredential.getKeypair().getPrivateKey()).build();
    }else{
      //password
      return LoginCredentialBuilder.newBuilder().username(loginCredential.getUsername()).password(loginCredential.getPassword()).build();
    }
  }
}
