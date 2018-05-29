package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.persistance.CloudDomainRepository;
import org.cloudiator.messages.Cloud.CloudDeletedResponse;
import org.cloudiator.messages.Cloud.DeleteCloudRequest;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteCloudSubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final CloudDomainRepository cloudDomainRepository;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCloudSubscriber.class);


  @Inject
  public DeleteCloudSubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository) {
    this.messageInterface = messageInterface;
    this.cloudDomainRepository = cloudDomainRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(DeleteCloudRequest.class, DeleteCloudRequest.parser(),
        (messageId, deleteCloudRequest) -> {
          try {
            doWork(messageId, deleteCloudRequest);
          } catch (Exception e) {
            LOGGER.error("Unexpected exception during cloud deletion. Error was " + e.getMessage(),
                e);
            messageInterface.reply(messageId, Error.newBuilder().setCode(500).setMessage(
                "Unexpected exception during cloud deletion. Error was " + e.getMessage()).build());
          }
        });
  }

  @Transactional
  void doWork(String messageId, DeleteCloudRequest deleteCloudRequest) {

    final String userId = deleteCloudRequest.getUserId();
    final String cloudId = deleteCloudRequest.getCloudId();

    final Cloud cloud = cloudDomainRepository.findByUserAndId(userId, cloudId);

    if (cloud == null) {
      messageInterface.reply(messageId, Error.newBuilder().setCode(404)
          .setMessage(String.format("Cloud with id %s does not exist.", userId)).build());
      return;
    }

    cloudDomainRepository.delete(cloud.id(), userId);

    messageInterface.reply(messageId, CloudDeletedResponse.newBuilder().build());

  }


}
