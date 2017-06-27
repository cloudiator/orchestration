package io.github.cloudiator.iaas.discovery.messaging;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.iaas.common.persistance.domain.CloudDomainRepository;
import io.github.cloudiator.iaas.common.persistance.messaging.converters.CloudMessageToCloudConverter;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messages.Cloud.CloudQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;

/**
 * Created by daniel on 09.06.17.
 */
public class CloudQuerySubscriber implements Runnable {

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

              //todo check if user exists?

              List<Cloud> clouds = cloudDomainRepository.findByUser(request.getUserId());
              CloudQueryResponse cloudQueryResponse = CloudQueryResponse.newBuilder()
                  .addAllClouds(clouds.stream().map(
                      cloudConverter::applyBack).collect(Collectors.toList())).build();
              messageInterface.reply(requestId, cloudQueryResponse);
            });
  }
}
