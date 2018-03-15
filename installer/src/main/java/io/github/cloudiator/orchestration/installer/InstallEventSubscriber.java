package io.github.cloudiator.orchestration.installer;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.orchestration.installer.remote.CompositeRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.KeyPairRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.PasswordRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.RemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.tools.installer.Installers;
import io.github.cloudiator.orchestration.installer.tools.installer.api.InstallApi;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Installation.InstallationRequest;
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

  private final NodeToNodeMessageConverter nodeToNodeMessageConverter = new NodeToNodeMessageConverter();


  @Inject
  public InstallEventSubscriber(MessageInterface messageInterface) {
    this.messagingService = messageInterface;
  }

  @Override
  public void run() {

    subscription = messagingService.subscribe(InstallationRequest.class,
        InstallationRequest.parser(), (requestId, InstallationRequest) -> {

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

    LOGGER.debug("Received installRequest with requestId: " + requestId);
    System.out.println("Received installRequest with requestId: " + requestId);

    Node node = nodeToNodeMessageConverter
        .applyBack(installationRequest.getInstallation().getNode());


    //checkState(!node.getId().isEmpty(),"No nodeId set for node: " + node.getId() + " !");

    RemoteConnectionStrategy remoteConnectionStrategy = new CompositeRemoteConnectionStrategy(Sets.newHashSet(
        new PasswordRemoteConnectionStrategy(), new KeyPairRemoteConnectionStrategy()));

    try {
      RemoteConnection remoteConnection = remoteConnectionStrategy.connect(node);

      installTools(remoteConnection, node, installationRequest.getUserId());

      LOGGER.debug("Established remote connection! Let's DD this node!");

    } catch (RemoteException e) {
      LOGGER.error("Unable to establish remote connection!", e);
    }

  }

  public void installTools(RemoteConnection remoteConnection, Node node, String userId){



    InstallApi installApi = Installers.of(remoteConnection, node, userId);


    try {

      LOGGER.debug("Remote connection established, starting to install Cloudiator tools...");
      installApi.installAll();

    } catch (RemoteException e) {
      LOGGER.error("Error while installing sources" , e);
    }

  }


  final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messagingService.reply(InstallationRequest.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }
}
