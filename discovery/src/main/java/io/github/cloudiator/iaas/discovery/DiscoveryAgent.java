package io.github.cloudiator.iaas.discovery;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.github.cloudiator.iaas.common.persistance.config.JpaModule;
import io.github.cloudiator.iaas.discovery.config.DiscoveryModule;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryAgent {

  private static Injector injector = Guice
      .createInjector(new DiscoveryModule(), new JpaModule(),
          new JpaPersistModule("defaultPersistenceUnit"));


  public static void main(String[] args) {

    final CloudAddedSubscriber instance = injector.getInstance(CloudAddedSubscriber.class);
    instance.run();
  }

}
