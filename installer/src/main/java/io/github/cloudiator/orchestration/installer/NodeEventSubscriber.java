package io.github.cloudiator.orchestration.installer;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.iaas.common.messaging.OperatingSystemConverter;
import io.github.cloudiator.orchestration.installer.remote.PasswordRemoteConnectionStrategy;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.NodeEntities.Node;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import io.github.cloudiator.orchestration.installer.remote.CompositeRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.KeyPairRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.RemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.tools.installer.UnixInstaller;
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



  private static final int SERVER_ERROR = 500;

  @Inject
  public NodeEventSubscriber(MessageInterface messageInterface) {
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

    checkState(!node.getUserId().isEmpty(),"No userId set for node: " + node.getId() + " !");


    OperatingSystem operatingSystem = new OperatingSystemConverter().apply(node.getNodeProperties().getOperationSystem());

    RemoteConnectionStrategy remoteConnectionStrategy = new CompositeRemoteConnectionStrategy(Sets.newHashSet(
        new PasswordRemoteConnectionStrategy(), new KeyPairRemoteConnectionStrategy()));

    try {
      RemoteConnection remoteConnection = remoteConnectionStrategy.connect(node, operatingSystem);

      installTools(remoteConnection, node);

      LOGGER.debug("Established remote connection! Let's DD this node!");

    } catch (RemoteException e) {
      LOGGER.error("Unable to establish remote connection!", e);
    }


  }

  public void installTools(RemoteConnection remoteConnection, Node node){

    UnixInstaller unixInstaller = new UnixInstaller(remoteConnection, node);

    try {

      LOGGER.debug("Remote connection established, starting to isntall clouiator tools...");
      unixInstaller.installAll();

    } catch (RemoteException e) {
      LOGGER.error("Error while installing sources" , e);
    }

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
