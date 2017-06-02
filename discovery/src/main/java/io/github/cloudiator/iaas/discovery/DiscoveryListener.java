package io.github.cloudiator.iaas.discovery;


/**
 * Created by daniel on 01.06.17.
 */
public interface DiscoveryListener {

  Class<?> interestedIn();

  void handle(Object o);

}
