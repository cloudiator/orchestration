package io.github.cloudiator.iaas.vm;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import org.cloudiator.messages.Cloud.CloudCreatedEvent;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;

public class CloudCreatedSubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final CloudRegistry cloudRegistry;
  private final CloudMessageToCloudConverter cloudMessageToCloudConverter = new CloudMessageToCloudConverter();

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

            if (!cloudRegistry.isRegistered(cloud)) {
              cloudRegistry.register(cloud);
            }
          }
        });
  }
}
