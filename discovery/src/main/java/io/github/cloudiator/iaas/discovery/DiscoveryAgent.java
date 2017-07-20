package io.github.cloudiator.iaas.discovery;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.github.cloudiator.iaas.common.persistance.config.JpaModule;
import io.github.cloudiator.iaas.discovery.config.DiscoveryModule;
import io.github.cloudiator.iaas.discovery.messaging.CloudAddedSubscriber;
import io.github.cloudiator.iaas.discovery.messaging.CloudQuerySubscriber;
import io.github.cloudiator.iaas.discovery.messaging.HardwareQuerySubscriber;
import io.github.cloudiator.iaas.discovery.messaging.ImageQuerySubscriber;
import io.github.cloudiator.iaas.discovery.messaging.LocationQuerySubscriber;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryAgent {

  private static Injector injector = Guice
      .createInjector(new DiscoveryModule(), new JpaModule(),
          new JpaPersistModule("defaultPersistenceUnit"), new KafkaMessagingModule());


  public static void main(String[] args) {

    final CloudAddedSubscriber instance = injector.getInstance(CloudAddedSubscriber.class);
    instance.run();

    ImageQuerySubscriber imageQuerySubscriber = injector.getInstance(ImageQuerySubscriber.class);
    imageQuerySubscriber.run();

    HardwareQuerySubscriber hardwareQuerySubscriber = injector
        .getInstance(HardwareQuerySubscriber.class);
    hardwareQuerySubscriber.run();

    LocationQuerySubscriber locationQuerySubscriber = injector
        .getInstance(LocationQuerySubscriber.class);
    locationQuerySubscriber.run();

    final CloudQuerySubscriber cloudQuerySubscriber = injector
        .getInstance(CloudQuerySubscriber.class);
    cloudQuerySubscriber.run();
  }

}
