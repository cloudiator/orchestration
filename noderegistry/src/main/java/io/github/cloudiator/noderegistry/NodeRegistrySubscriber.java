package io.github.cloudiator.noderegistry;

import javax.inject.Inject;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messages.NodeEntities.Node;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeRegistrySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistrySubscriber.class);

  private final MessageInterface messagingService;
  private final NodeRegistry<byte[]> registry;
  private volatile Subscription subscription;

  @Inject
  public NodeRegistrySubscriber(MessageInterface messageInterface, CloudService cloudService,
      NodeRegistry<byte[]> registry) {
    this.messagingService = messageInterface;
    this.registry = registry;
  }

  @Override
  public void run() {
    subscription = messagingService.subscribe(NodeEvent.class,
        NodeEvent.parser(), (eventId, nodeEvent) -> {

          LOGGER.error("message received: " + eventId);

          if (null == nodeEvent.getNodeStatus()) {
            IllegalArgumentException ex = new IllegalArgumentException("status not set: null");
            LOGGER.error("cannot start virtual machine.", ex);
            throw ex;
          }

          try {
            switch (nodeEvent.getNodeStatus()) {
              case CREATED:
                NodeRegistrySubscriber.LOGGER.info("processing CREATED event.");
                handleCreation(nodeEvent.getNode());
                break;
              case DELETED:
                NodeRegistrySubscriber.LOGGER.info("processing DELETED event.");
                handleDeletion(nodeEvent.getNode());
                break;
              case UNKNOWN_STATUS:
                NodeRegistrySubscriber.LOGGER.error("processing UNDEFINED event.");
                throw new IllegalArgumentException(
                    "status not set: " + nodeEvent.getNode());
              default:
                NodeRegistrySubscriber.LOGGER.error("unknown event type.");
                throw new IllegalArgumentException(
                    "status unknown: " + nodeEvent.getNodeStatus());
            }
          } catch (Exception ex) {
            LOGGER.error("exception occurred when handling virtual machine event.", ex);
          }
        });
  }

  synchronized void handleDeletion(Node node) throws RegistryException {
    String vmId = node.getId();
    registry.remove(vmId);
  }

  synchronized void handleCreation(Node node) throws RegistryException {
    String vmId = node.getId();
    registry.put(vmId, node.toByteArray());
  }

  void terminate() {
    if (subscription != null) {
      subscription.cancel();
    }
  }
}
