package io.github.cloudiator.iaas.discovery;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.github.cloudiator.iaas.common.persistance.config.JpaModule;
import io.github.cloudiator.iaas.discovery.config.DiscoveryModule;
import org.cloudiator.messages.Cloud.CreateCloudRequest;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messages.Hardware.HardwareQueryResponse;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messages.Image.ImageQueryResponse;
import org.cloudiator.messages.entities.IaasEntities.Api;
import org.cloudiator.messages.entities.IaasEntities.CloudType;
import org.cloudiator.messages.entities.IaasEntities.Configuration;
import org.cloudiator.messages.entities.IaasEntities.Credential;
import org.cloudiator.messages.entities.IaasEntities.NewCloud;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.CloudServiceImpl;
import org.cloudiator.messaging.services.HardwareService;
import org.cloudiator.messaging.services.HardwareServiceImpl;
import org.cloudiator.messaging.services.ImageService;
import org.cloudiator.messaging.services.ImageServiceImpl;

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
    final CloudQuerySubscriber cloudQuerySubscriber = injector.getInstance(CloudQuerySubscriber.class);
    cloudQuerySubscriber.run();


  }

}
