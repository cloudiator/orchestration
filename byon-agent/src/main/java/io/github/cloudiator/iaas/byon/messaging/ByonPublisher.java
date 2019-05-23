package io.github.cloudiator.iaas.byon.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import org.cloudiator.messages.Byon.ByonData;
import org.cloudiator.messages.Byon.ByonNodeEvent;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ByonPublisher {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonPublisher.class);
  private final MessageInterface messageInterface;

  @Inject
  public ByonPublisher(MessageInterface messageInterface) {
    this.messageInterface = messageInterface;
  }

  public final void publishEvent(ByonData data) {
    final String state = data.getAllocated() ? "allocated" : "unallocated";
    LOGGER.info(String.format("Publishing state change for byon %s."
        + "New state is %s", data.getName(), state));
    messageInterface.publish(ByonNodeEvent.newBuilder()
        .setByonNode(ByonOperations.buildMessageNode(data))
        .build());
  }

}
