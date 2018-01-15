package io.github.cloudiator.iaas.discovery.messaging;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.iaas.common.messaging.converters.CloudMessageToCloudConverter;
import io.github.cloudiator.iaas.common.persistance.domain.CloudDomainRepository;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messages.Cloud.CloudQueryResponse;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 09.06.17.
 */
public class CloudQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloudQuerySubscriber.class);
  private final MessageInterface messageInterface;
  private final CloudDomainRepository cloudDomainRepository;
  private final CloudMessageToCloudConverter cloudConverter = new CloudMessageToCloudConverter();

  @Inject
  public CloudQuerySubscriber(MessageInterface messageInterface,
      CloudDomainRepository cloudDomainRepository) {
    checkNotNull(cloudDomainRepository, "cloudDomainRepository is null");
    this.cloudDomainRepository = cloudDomainRepository;
    checkNotNull(messageInterface, "messageInterface is null");
    this.messageInterface = messageInterface;
  }

  @Override
  public void run() {
    Subscription subscription = messageInterface
        .subscribe(CloudQueryRequest.class, CloudQueryRequest.parser(),
            (requestId, request) -> {

              try {
                decideAndReply(requestId, request);
              } catch (Exception e) {
                LOGGER.error(
                    String.format("Exception %s caught while execution %s", e.getMessage(), this),
                    e);
              }
            });
  }

  private void decideAndReply(String requestId, CloudQueryRequest request) {
    if (request.getUserId().isEmpty()) {
      replyErrorNoUserId(requestId);
      return;
    }
    if (!request.getCloudId().isEmpty()) {
      replyForUserIdAndCloudId(requestId, request.getUserId(), request.getCloudId());
      return;
    }

    replyForUserId(requestId, request.getUserId());
  }

  private void replyErrorNoUserId(String requestId) {
    messageInterface.reply(CloudQueryResponse.class, requestId,
        Error.newBuilder().setCode(500).setMessage("Request does not contain userId.")
            .build());
  }

  private void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    final Cloud cloud = cloudDomainRepository
        .findByUserAndId(userId, cloudId);
    if (cloud == null) {
      messageInterface.reply(CloudQueryResponse.class, requestId,
          Error.newBuilder().setCode(404)
              .setMessage(String.format("Cloud with id %s was not found.", cloudId))
              .build());
    } else {
      CloudQueryResponse cloudQueryResponse = CloudQueryResponse.newBuilder()
          .addClouds(cloudConverter.applyBack(cloud)).build();
      messageInterface.reply(requestId, cloudQueryResponse);
    }

  }

  private void replyForUserId(String requestId, String userId) {
    CloudQueryResponse cloudQueryResponse = CloudQueryResponse.newBuilder()
        .addAllClouds(cloudDomainRepository.findByUser(userId).stream().map(
            cloudConverter::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, cloudQueryResponse);
  }

}
