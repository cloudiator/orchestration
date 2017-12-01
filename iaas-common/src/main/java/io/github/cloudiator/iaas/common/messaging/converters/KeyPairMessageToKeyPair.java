package io.github.cloudiator.iaas.common.messaging.converters;

import de.uniulm.omi.cloudiator.sword.domain.KeyPair;
import de.uniulm.omi.cloudiator.sword.domain.KeyPairBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class KeyPairMessageToKeyPair implements TwoWayConverter<IaasEntities.KeyPair, KeyPair> {

  @Override
  public IaasEntities.KeyPair applyBack(KeyPair keyPair) {

    IaasEntities.KeyPair.newBuilder().setPrivateKey(keyPair.privateKey().get())
        .setPublicKey(keyPair.publicKey()).build();

    return null;
  }

  @Override
  public KeyPair apply(IaasEntities.KeyPair keyPair) {

    return KeyPairBuilder.newBuilder().privateKey(keyPair.getPrivateKey())
        .publicKey(keyPair.getPublicKey()).build();

  }
}
