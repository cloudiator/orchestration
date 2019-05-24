package io.github.cloudiator.iaas.byon.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.iaas.byon.util.ByonOperations;
import io.github.cloudiator.domain.ByonIO;
import io.github.cloudiator.messaging.ByonOperationConverter;
import org.cloudiator.messages.Byon.ByonData;
import org.cloudiator.messages.Byon.ByonNodeEvent;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ByonPublisher {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonPublisher.class);
  private static final ByonOperationConverter BYON_OPERATION_CONVERTER =
      new ByonOperationConverter();
  private final MessageInterface messageInterface;

  @Inject
  public ByonPublisher(MessageInterface messageInterface) {
    this.messageInterface = messageInterface;
  }

  public final void publishEvent(ByonData data, ByonIO operation) {
    LOGGER.info(String.format("Publishing new state for byon %s"
        + "in the system: %s", data.getName(), operation));
    messageInterface.publish(ByonNodeEvent.newBuilder()
        .setByonNode(ByonOperations.buildMessageNode(data))
        .setOperation(BYON_OPERATION_CONVERTER.apply(operation))
        .build());
  }
}
