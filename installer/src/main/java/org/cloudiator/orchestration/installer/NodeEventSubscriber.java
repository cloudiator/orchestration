package org.cloudiator.orchestration.installer;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.iaas.common.messaging.OperatingSystemConverter;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.NodeOuterClass.Node;
import org.cloudiator.messages.NodeOuterClass.NodeEvent;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.services.CloudService;
import org.cloudiator.orchestration.installer.remote.CompositeRemoteConnectionStrategy;
import org.cloudiator.orchestration.installer.remote.KeyPairRemoteConnectionStrategy;
import org.cloudiator.orchestration.installer.remote.PasswordRemoteConnectionStrategy;
import org.cloudiator.orchestration.installer.remote.RemoteConnectionStrategy;
import org.cloudiator.orchestration.installer.tools.installer.UnixInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class NodeEventSubscriber implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(NodeEventSubscriber.class);

  private final MessageInterface messagingService;
  private volatile Subscription subscription;


  private static final int ILLEGAL_CLOUD_ID = 400;
  private static final int SERVER_ERROR = 500;

  @Inject
  public NodeEventSubscriber(MessageInterface messageInterface,
      CloudService cloudService) {
    this.messagingService = messageInterface;
  }

  @Override
  public void run() {

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

  }

  final void handleRequest(String requestId, Node node){

    /*
    RemoteConnectionStrategy remoteConnectionStrategy = CompositeRemoteConnectionStrategy.RemoteConnectionStrategiesFactory(
        Sets.newHashSet(injector.getInstance(
            KeyPairRemoteConnectionStrategy.KeyPairRemoteConnectionStrategyFactory.class),
            injector.getInstance(
                PasswordRemoteConnectionStrategy.PasswordRemoteConnectionStrategyFactory.class)));;

    */


    //if(operatingSystem.operatingSystemFamily().operatingSystemType().equals(OperatingSystemType.WINDOWS))



    OperatingSystem operatingSystem = new OperatingSystemConverter().apply(node.getNodeProperties().getOperationSystem());

    RemoteConnectionStrategy remoteConnectionStrategy = new CompositeRemoteConnectionStrategy(Sets.newHashSet(
        new PasswordRemoteConnectionStrategy(), new KeyPairRemoteConnectionStrategy()));

    try {
      RemoteConnection remoteConnection = remoteConnectionStrategy.connect(node, operatingSystem);

      LOGGER.debug("Established remote connection! Let's DD this node!");

    } catch (RemoteException e) {
      LOGGER.error("Unable to establish remote connection!", e);
    }


  }

  public void installTools(RemoteConnection remoteConnection, Node node, String userId){

    UnixInstaller unixInstaller = new UnixInstaller(remoteConnection, node, userId);

  }


  final void sendSuccessResponse(String messageId, VirtualMachine vm) {
   // messagingService.reply(messageId,
     //   VirtualMachineCreatedResponse.newBuilder().setVirtualMachine(vm).build());
    //TODO
  }

  final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messagingService.reply(NodeEvent.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
