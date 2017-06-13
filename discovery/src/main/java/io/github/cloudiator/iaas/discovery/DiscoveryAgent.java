package io.github.cloudiator.iaas.discovery;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.github.cloudiator.iaas.common.persistance.config.JpaModule;
import io.github.cloudiator.iaas.discovery.config.DiscoveryModule;
import io.github.cloudiator.iaas.discovery.messaging.CloudAddedSubscriber;
import io.github.cloudiator.iaas.discovery.messaging.HardwareQuerySubscriber;
import io.github.cloudiator.iaas.discovery.messaging.ImageQuerySubscriber;
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
          new JpaPersistModule("defaultPersistenceUnit"));


  public static void main(String[] args) {

    final CloudAddedSubscriber instance = injector.getInstance(CloudAddedSubscriber.class);
    instance.run();
    ImageQuerySubscriber imageQuerySubscriber = injector.getInstance(ImageQuerySubscriber.class);
    imageQuerySubscriber.run();
    HardwareQuerySubscriber hardwareQuerySubscriber = injector
        .getInstance(HardwareQuerySubscriber.class);
    hardwareQuerySubscriber.run();

    final MessageInterface instance1 = injector.getInstance(MessageInterface.class);

    Api api = Api.newBuilder().setProviderName("openstack-nova").build();
    Credential credential = Credential.newBuilder().setUser("***REMOVED***")
        .setSecret("***REMOVED***").build();
    Configuration configuration = Configuration.newBuilder().setNodeGroup("nodegroup").build();
    NewCloud newCloud = NewCloud.newBuilder().setApi(api).setCloudType(CloudType.PRIVATE)
        .setConfiguration(configuration).setCredential(credential)
        .setEndpoint("***REMOVED***").build();

    CreateCloudRequest createCloudRequest = CreateCloudRequest.newBuilder().setUserId("blub")
        .setCloud(newCloud).build();

    CloudServiceImpl cloudService = new CloudServiceImpl(instance1);
    try {
      cloudService.createCloud(createCloudRequest);
    } catch (ResponseException e) {
      e.printStackTrace();
    }

    while (true) {
      ImageService imageService = new ImageServiceImpl(instance1);
      HardwareService hardwareService = new HardwareServiceImpl(instance1);
      try {
        ImageQueryResponse imageQueryResponse = imageService
            .getImages(ImageQueryRequest.newBuilder().setUserId("blub").build());
        imageQueryResponse.getImagesList().forEach(System.out::println);
        HardwareQueryResponse hardwareQueryResponse = hardwareService
            .getHardware(HardwareQueryRequest.newBuilder().setUserId("blub").build());
        hardwareQueryResponse.getHardwareFlavorsList().forEach(System.out::println);
      } catch (ResponseException e) {
        e.printStackTrace();
      }
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
