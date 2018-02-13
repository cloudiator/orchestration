package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.iaas.discovery.AbstractDiscoveryWorker;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import io.github.cloudiator.messaging.NewCloudMessageToCloud;
import io.github.cloudiator.persistance.CloudDomainRepository;
import javax.persistence.EntityManager;
import org.cloudiator.messages.Cloud.CloudCreatedResponse;
import org.cloudiator.messages.Cloud.CreateCloudRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudAddedSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiscoveryWorker.class);

  private final MessageInterface messageInterface;
  private final CloudDomainRepository cloudDomainRepository;
  private final UnitOfWork unitOfWork;
  private final Provider<EntityManager> entityManager;
  private final NewCloudMessageToCloud newCloudConverter;
  private final CloudMessageToCloudConverter cloudConverter;

  @Inject
  public CloudAddedSubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository,
      UnitOfWork unitOfWork,
      Provider<EntityManager> entityManager,
      NewCloudMessageToCloud newCloudConverter,
      CloudMessageToCloudConverter cloudConverter) {
    this.messageInterface = messageInterface;
    this.cloudDomainRepository = cloudDomainRepository;
    this.unitOfWork = unitOfWork;
    this.entityManager = entityManager;
    this.newCloudConverter = newCloudConverter;
    this.cloudConverter = cloudConverter;
  }

  @Override
  public void run() {

    final Subscription subscription = messageInterface
        .subscribe(CreateCloudRequest.class, CreateCloudRequest.parser(),
            (messageId, createCloudRequest) -> {

              //start the transaction
              unitOfWork.begin();
              entityManager.get().getTransaction().begin();

              try {

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

                  //repy
                  messageInterface.reply(messageId,
                      CloudCreatedResponse.newBuilder()
                          .setCloud(cloudConverter.applyBack(cloudToBeCreated)).build());
                  entityManager.get().getTransaction().commit();
                }
              } catch (Exception e) {
                LOGGER.error(String.format("Exception occurred during handling of message %s.",
                    createCloudRequest), e);
                messageInterface.reply(CloudCreatedResponse.class, messageId, Error.newBuilder()
                    .setMessage(String
                        .format("Could not understand request %s. An %s exception occurred: %s.",
                            createCloudRequest, e.getClass().getName(), e.getMessage()))
                    .setCode(500).build());
                entityManager.get().getTransaction().rollback();
              } finally {
                unitOfWork.end();
              }
            });
  }
}
