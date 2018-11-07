package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.iaas.discovery.CloudStateMachine;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import io.github.cloudiator.messaging.InitializeCloudFromNewCloud;
import io.github.cloudiator.persistance.CloudDomainRepository;
import java.util.concurrent.ExecutionException;
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
  private static final InitializeCloudFromNewCloud INITIALIZE_CLOUD_FROM_NEW_CLOUD = InitializeCloudFromNewCloud.INSTANCE;
  private static final CloudMessageToCloudConverter CLOUD_CONVERTER = CloudMessageToCloudConverter.INSTANCE;
  private final CloudService cloudService;
  private final CloudStateMachine cloudStateMachine;

  @Inject
  public CloudAddedSubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository,
      CloudService cloudService,
      CloudStateMachine cloudStateMachine) {
    this.messageInterface = messageInterface;
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudService = cloudService;
    this.cloudStateMachine = cloudStateMachine;
  }

  @Override
  public void run() {

    final Subscription subscription = messageInterface
        .subscribe(CreateCloudRequest.class, CreateCloudRequest.parser(),
            this::doWork);
  }

  private void doWork(String messageId, CreateCloudRequest createCloudRequest) {

    try {

      ExtendedCloud cloudToBeCreated = INITIALIZE_CLOUD_FROM_NEW_CLOUD
          .apply(createCloudRequest.getCloud(), createCloudRequest.getUserId());

      if (exists(cloudToBeCreated)) {
        messageInterface.reply(CloudCreatedResponse.class, messageId,
            Error.newBuilder().setCode(409).setMessage(String
                .format("The cloud %s is already registered",
                    cloudToBeCreated)).build());
      }

      try {
        cloudStateMachine.apply(cloudToBeCreated, CloudState.OK);
      } catch (ExecutionException e) {
        if (e.getCause() instanceof Exception) {
          throw (Exception) e.getCause();
        }
        throw e;
      }


    } catch (Exception e) {
      LOGGER.error(String.format("Unexpected exception occurred during handling of request %s.",
          createCloudRequest), e);
      messageInterface.reply(CloudCreatedResponse.class, messageId, Error.newBuilder()
          .setMessage(String
              .format("Unexpected exception occurred during handling of request %s: %s.",
                  createCloudRequest, e.getMessage()))
          .setCode(500).build());
    }
  }


  @Transactional
  private boolean exists(ExtendedCloud extendedCloud) {
    if (cloudDomainRepository.findById(extendedCloud.id()) != null) {
      return true;
    }
    return false;
  }

}
