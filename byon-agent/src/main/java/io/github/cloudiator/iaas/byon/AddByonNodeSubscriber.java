package io.github.cloudiator.iaas.byon;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.inject.Inject;
import org.cloudiator.messages.Byon.AddByoNodeRequest;
import org.cloudiator.messages.Byon.ByoNode;
import org.cloudiator.messages.Byon.ByoNodeAddedResponse;
import org.cloudiator.messages.Byon.ByonData;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messages.NodeEntities.Node;
import org.cloudiator.messages.NodeEntities.NodeProperties;
import org.cloudiator.messages.NodeEntities.NodeType;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messages.entities.IaasEntities.Location;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddByonNodeSubscriber implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AddByonNodeSubscriber.class);
  private static final int ILLEGAL_CLOUD_ID = 400;
  private static final int SERVER_ERROR = 500;
  private final MessageInterface messagingService;
  // private final CloudService cloudService;
  private volatile Subscription subscription;

  @Inject
  public AddByonNodeSubscriber(MessageInterface messageInterface) {
    this.messagingService = messageInterface;
  }

  @Override
  public void run() {
    subscription = messagingService.subscribe(AddByoNodeRequest.class,
        AddByoNodeRequest.parser(), (requestId, request) -> {
          try {
            ByonData data = request.getByonRequest();
            ByoNode node = AddByonNodeSubscriber.this
                .handleRequest(requestId, request.getUserId(), data);
            publishCreationEvent(node.getId(), data);
          } catch (Exception ex) {
            LOGGER.error("exception occurred.", ex);
            AddByonNodeSubscriber.this.sendErrorResponse(requestId,
                "exception occurred: " + ex.getMessage(), SERVER_ERROR);
          }
        });
  }

  private final void publishCreationEvent(String nodeId, ByonData data) {
    messagingService.publish(NodeEvent.newBuilder()
        .setNode(Node.newBuilder().
            addAllIpAddresses(data.getIpAddressList()).
            setLoginCredential(data.getLoginCredentials()).
            setNodeProperties(data.getProperties()).
            setNodeType(NodeType.BYON).
            setId(nodeId).
            build()).
            //setNodeStatus(NodeStatus.CREATED).
            build());

  }

  private ByoNode handleRequest(String requestId, String userId, ByonData byonRequest) {
    LOGGER.info("byo node creating unique identifier");
    String nodeId = createId(byonRequest);
    ByoNode node = ByoNode.newBuilder().setNodeData(byonRequest).setId(nodeId).build();
    LOGGER.info("byo node registered. sending response");
    sendSuccessResponse(requestId, node);
    LOGGER.info("response sent.");
    return node;
  }

  private final String createId(ByonData data) {
    String result = "";
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      //digestLocation(md, data.getProperties().getLocation());
      byte[] digest = md.digest();
      BigInteger bigInt = new BigInteger(1, digest);
      result = bigInt.toString(16);
    } catch (NoSuchAlgorithmException ex) {
      LOGGER.error("cannot digest location, using random integer ", ex);
      result = UUID.randomUUID().toString();
    }
    return result;
  }

  private final void digestHardware(MessageDigest md, NodeProperties prop)
      throws UnsupportedEncodingException {
    md.update(String.valueOf(prop.getNumberOfCores()).getBytes("UTF-8"));
    md.update(String.valueOf(prop.getMemory()).getBytes("UTF-8"));
    md.update(String.valueOf(prop.getDisk()).getBytes("UTF-8"));
  }

  private final void digestLocation(MessageDigest md, Location loc)
      throws UnsupportedEncodingException {
    if (loc.getName() != null) {
      md.update(loc.getName().getBytes("UTF-8"));
    }
    if (loc.getLocationScope() != null) {
      md.update(loc.getLocationScope().name().getBytes("UTF-8"));
    }
    if (loc.getParent() != null) {
      digestLocation(md, loc.getParent());
    }
  }

  private final void sendSuccessResponse(String messageId, ByoNode node) {
    messagingService.reply(messageId,
        ByoNodeAddedResponse.newBuilder().setByoNode(node).build());
  }

  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messagingService.reply(VirtualMachineCreatedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }

  void terminate() {
    if (subscription != null) {
      subscription.cancel();
    }
  }
}
