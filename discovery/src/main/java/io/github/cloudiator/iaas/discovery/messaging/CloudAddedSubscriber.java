package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.entities.Tenant;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepository;
import io.github.cloudiator.iaas.discovery.AbstractDiscoveryWorker;
import io.github.cloudiator.iaas.common.persistance.messaging.converters.CloudMessageToCloudConverter;
import io.github.cloudiator.iaas.common.persistance.messaging.converters.NewCloudMessageToCloud;
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
  private final CloudRegistry cloudRegistry;
  private final NewCloudMessageToCloud newCloudConverter;
  private final CloudMessageToCloudConverter cloudConverter;
  private final UnitOfWork unitOfWork;
  private final TenantModelRepository tenantModelRepository;
  private final CloudModelRepository cloudModelRepository;
  private final Provider<EntityManager> entityManager;

  @Inject
  public CloudAddedSubscriber(MessageInterface messageInterface,
      CloudRegistry cloudRegistry,
      NewCloudMessageToCloud newCloudConverter,
      CloudMessageToCloudConverter cloudConverter,
      UnitOfWork unitOfWork,
      TenantModelRepository tenantModelRepository,
      CloudModelRepository cloudModelRepository,
      Provider<EntityManager> entityManager) {
    this.messageInterface = messageInterface;
    this.cloudRegistry = cloudRegistry;
    this.newCloudConverter = newCloudConverter;
    this.cloudConverter = cloudConverter;
    this.unitOfWork = unitOfWork;
    this.tenantModelRepository = tenantModelRepository;
    this.cloudModelRepository = cloudModelRepository;
    this.entityManager = entityManager;
  }

  @Override
  public void run() {

    final Subscription subscription = messageInterface
        .subscribe(CreateCloudRequest.class, CreateCloudRequest.parser(),
            (messageId, createCloudRequest) -> {

              //create the tenant if it does not already exist
              unitOfWork.begin();
              entityManager.get().getTransaction().begin();
              try {
                Cloud cloud = newCloudConverter.apply(createCloudRequest.getCloud());
                Tenant tenant = tenantModelRepository.createOrGet(createCloudRequest.getUserId());
                CloudModel cloudModelEntity = cloudModelRepository
                    .getByCloudId(cloud.id());

                if (cloudModelEntity != null) {
                  if (!cloudModelEntity.getTenant().equals(tenant)) {
                    //reply with error
                    messageInterface.reply(CloudCreatedResponse.class, messageId,
                        Error.newBuilder().setCode(409).setMessage(String
                            .format("The cloud %s is already registered with another tenant %s.",
                                cloud,
                                tenant)).build());
                  }
                } else {
                  cloudModelEntity = new CloudModel(
                      cloud.id(), tenant);
                  cloudModelRepository.save(cloudModelEntity);
                }

                if (cloudRegistry.isRegistered(cloud)) {
                  messageInterface.reply(CloudCreatedResponse.class, messageId,
                      Error.newBuilder().setCode(409)
                          .setMessage(String.format("The cloud %s already exists.", cloud))
                          .build());
                }
                cloudRegistry.register(cloud);
                messageInterface.reply(messageId,
                    CloudCreatedResponse.newBuilder().setCloud(cloudConverter.applyBack(cloud))
                        .build());
                entityManager.get().getTransaction().commit();
              } catch (Exception e) {
                LOGGER.error(String.format("Exception occurred during handling of message %s.",
                    createCloudRequest), e);
                messageInterface.reply(CloudCreatedResponse.class, messageId, Error.newBuilder()
                    .setMessage(String
                        .format("Could not understand request. An %s exception occurred: %s.",
                            e.getClass().getName(), e.getMessage())).setCode(500).build());
                entityManager.get().getTransaction().rollback();
              } finally {
                unitOfWork.end();
              }
            });
  }
}
