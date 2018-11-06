package io.github.cloudiator.iaas.vm.messaging;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import org.cloudiator.messages.Cloud.CloudCreatedEvent;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CloudCreatedSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CloudCreatedSubscriber.class);
  private final MessageInterface messageInterface;
  private final CloudRegistry cloudRegistry;
  private final CloudMessageToCloudConverter cloudMessageToCloudConverter = CloudMessageToCloudConverter.INSTANCE;

  @Inject
  public CloudCreatedSubscriber(MessageInterface messageInterface,
      CloudRegistry cloudRegistry) {
    this.messageInterface = messageInterface;
    this.cloudRegistry = cloudRegistry;
  }

  @Override
  public void run() {
    messageInterface.subscribe(CloudCreatedEvent.class, CloudCreatedEvent.parser(),
        new MessageCallback<CloudCreatedEvent>() {
          @Override
          public void accept(String id, CloudCreatedEvent content) {

            final Cloud cloud = cloudMessageToCloudConverter.apply(content.getCloud());
            LOGGER.info(String
                .format("%s is receiving new cloud created event for cloud %s.", this, cloud));

            if (!cloudRegistry.isRegistered(cloud)) {
              LOGGER.debug(
                  String.format("%s is registering new cloud %s to cloud registry", this, cloud));
              cloudRegistry.register(cloud);
            }
          }
        });
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
