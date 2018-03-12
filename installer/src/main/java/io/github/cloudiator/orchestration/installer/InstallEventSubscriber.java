package io.github.cloudiator.orchestration.installer;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.iaas.common.messaging.OperatingSystemConverter;
import io.github.cloudiator.orchestration.installer.remote.CompositeRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.KeyPairRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.PasswordRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.RemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.tools.installer.UnixInstaller;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.Node;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 12.09.2017.
 */
public class InstallEventSubscriber implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(InstallEventSubscriber.class);

  private final MessageInterface messagingService;
  private volatile Subscription subscription;



  private static final int SERVER_ERROR = 500;


  @Inject
  public InstallEventSubscriber(MessageInterface messageInterface) {
    this.messagingService = messageInterface;
  }

  @Override
  public void run() {

    subscription = messagingService.subscribe(InstallationRequest.class,
        InstallationRequest.parser(), (requestId, InstallationRequest) -> {
          //Node node  = NodeEvent.getNode();



          try {
            InstallEventSubscriber.this.handleRequest(requestId,
                InstallationRequest);
          } catch (Exception ex) {
            LOGGER.error("exception occurred.", ex);
            InstallEventSubscriber.this.sendErrorResponse(requestId,
                "exception occurred: " + ex.getMessage(), SERVER_ERROR);
          }
        });

  }

  final void handleRequest(String requestId, InstallationRequest installationRequest){

    //TODO: change from NodeEntities to Domain Object
    //TODO: pass the selected tools for the installation to the installer
    //TODO: implement queue + worker as done for NodeEvent to enable mutlipe installations in parallel

    NodeEntities.Node node = installationRequest.getInstallation().getNode();



    OperatingSystem operatingSystem = new OperatingSystemConverter().apply(node.getNodeProperties().getOperationSystem());

    checkState(!node.getUserId().isEmpty(),"No userId set for node: " + node.getId() + " !");

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

      LOGGER.debug("Remote connection established, starting to install Cloudiator tools...");
      unixInstaller.installAll();

    } catch (RemoteException e) {
      LOGGER.error("Error while installing sources" , e);
    }

  }


  final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messagingService.reply(NodeEvent.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
