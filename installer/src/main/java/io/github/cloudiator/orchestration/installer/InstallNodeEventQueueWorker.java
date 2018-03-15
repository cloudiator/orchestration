package io.github.cloudiator.orchestration.installer;

import com.google.inject.Inject;
import io.github.cloudiator.orchestration.installer.InstallNodeEventQueue.NodeEventItem;
import org.cloudiator.messages.NodeEntities.Node;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 20.07.2017.
 */
public class InstallNodeEventQueueWorker implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(InstallNodeEventQueueWorker.class);

  private final InstallNodeEventQueue installNodeEventQueue;

  @Inject
  InstallNodeEventQueueWorker(InstallNodeEventQueue installNodeEventQueue) {
    this.installNodeEventQueue = installNodeEventQueue;
  }

  @Override
  public void run() {

    while (!Thread.currentThread().isInterrupted()) {

      try {
        NodeEventItem nodeEventItem = this.installNodeEventQueue.take();

        this.handleRequest(nodeEventItem.requestId(), nodeEventItem.node());


      } catch (InterruptedException e) {
        LOGGER.error("Error while fetich items from local NodeEventItem queue", e);
      }


    }

    /*
    subscription = messagingService.subscribe(NodeEvent.class,
        NodeEvent.parser(), (requestId, NodeEvent) -> {
          Node node  = NodeEvent.getNode();

          try {
            NodeEventSubscriber.this.handleRequest(requestId,
                node);
          } catch (Exception ex) {
            LOGGER.error("exception occurred.", ex);
            NodeEventSubscriber.this.sendErrorResponse(requestId,
                "exception occurred: " + ex.getMessage(), SERVER_ERROR);
          }
        });

        */

  }

  final void handleRequest(String requestId, Node node) {

    /*

    OperatingSystem operatingSystem = new OperatingSystemConverter()
        .apply(node.getNodeProperties().getOperationSystem());

    RemoteConnectionStrategy remoteConnectionStrategy = new CompositeRemoteConnectionStrategy(
        Sets.newHashSet(
            new PasswordRemoteConnectionStrategy(), new KeyPairRemoteConnectionStrategy()));

    try {
      RemoteConnection remoteConnection = remoteConnectionStrategy.connect(node, operatingSystem);

      installTools(remoteConnection, node);

      LOGGER.debug("Established remote connection! Let's DD this node!");

    } catch (RemoteException e) {
      LOGGER.error("Unable to establish remote connection!", e);
    }

    */


  }



  final void sendSuccessResponse(String messageId, VirtualMachine vm) {
    // messagingService.reply(messageId,
    //   VirtualMachineCreatedResponse.newBuilder().setVirtualMachine(vm).build());
    //TODO
  }

  /*
  final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messagingService.reply(NodeEvent.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
  */
}
