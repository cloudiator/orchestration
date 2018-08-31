package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import io.github.cloudiator.messaging.NewCloudMessageToCloud;
import io.github.cloudiator.persistance.CloudDomainRepository;
import org.cloudiator.messages.Cloud.CloudCreatedEvent;
import org.cloudiator.messages.Cloud.CloudCreatedResponse;
import org.cloudiator.messages.Cloud.CreateCloudRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudAddedSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloudAddedSubscriber.class);

  private final MessageInterface messageInterface;
  private final CloudDomainRepository cloudDomainRepository;
  private static final NewCloudMessageToCloud newCloudConverter = NewCloudMessageToCloud.INSTANCE;
  private static final CloudMessageToCloudConverter CLOUD_CONVERTER = CloudMessageToCloudConverter.INSTANCE;
  private final CloudService cloudService;

  @Inject
  public CloudAddedSubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository,
      CloudService cloudService) {
    this.messageInterface = messageInterface;
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudService = cloudService;
  }

  @Override
  public void run() {

    final Subscription subscription = messageInterface
        .subscribe(CreateCloudRequest.class, CreateCloudRequest.parser(),
            this::doWork);
  }

  private void doWork(String messageId, CreateCloudRequest createCloudRequest) {

    try {
      importCloud(messageId, createCloudRequest);
    } catch (Exception e) {
      LOGGER.error(String.format("Exception occurred during handling of message %s.",
          createCloudRequest), e);
      messageInterface.reply(CloudCreatedResponse.class, messageId, Error.newBuilder()
          .setMessage(String
              .format("Could not understand request %s. An %s exception occurred: %s.",
                  createCloudRequest, e.getClass().getName(), e.getMessage()))
          .setCode(500).build());
    }
  }

  @Transactional
  void importCloud(String messageId, CreateCloudRequest createCloudRequest) {
    //create the cloud object from the message
    Cloud cloudToBeCreated = newCloudConverter.apply(createCloudRequest.getCloud());

    //check if the cloud already exists
    if (cloudDomainRepository.findById(cloudToBeCreated.id()) != null) {
      //reply with error
      messageInterface.reply(CloudCreatedResponse.class, messageId,
          Error.newBuilder().setCode(409).setMessage(String
              .format("The cloud %s is already registered",
                  cloudToBeCreated)).build());
    } else {

      //create the cloud
      cloudDomainRepository.save(cloudToBeCreated, createCloudRequest.getUserId());

      //reply
      messageInterface.reply(messageId,
          CloudCreatedResponse.newBuilder()
              .setCloud(CLOUD_CONVERTER.applyBack(cloudToBeCreated)).build());
      //emit event
      cloudService.cloudCreatedEvent(
          CloudCreatedEvent.newBuilder().setCloud(CLOUD_CONVERTER.applyBack(cloudToBeCreated))
              .setUserId(createCloudRequest.getUserId()).build());

    }
  }

}
