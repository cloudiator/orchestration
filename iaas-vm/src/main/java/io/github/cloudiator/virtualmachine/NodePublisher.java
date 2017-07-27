package io.github.cloudiator.virtualmachine;

import static com.google.common.base.Preconditions.checkState;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.cloudiator.iaas.common.messaging.IpAddressMessageToIpAddress;
import io.github.cloudiator.iaas.common.messaging.LoginCredentialMessageToLoginCredentialConverter;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messages.NodeOuterClass.Node;
import org.cloudiator.messages.NodeOuterClass.NodeEvent;
import org.cloudiator.messages.NodeOuterClass.NodeProperties;
import org.cloudiator.messages.NodeOuterClass.NodeStatus;
import org.cloudiator.messages.NodeOuterClass.NodeType;
import org.cloudiator.messages.entities.IaasEntities.HardwareFlavor;
import org.cloudiator.messages.entities.IaasEntities.Image;
import org.cloudiator.messages.entities.IaasEntities.IpAddressType;
import org.cloudiator.messages.entities.IaasEntities.Location;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.HardwareService;
import org.cloudiator.messaging.services.ImageService;
import org.cloudiator.messaging.services.LocationService;

public class NodePublisher {

  private final MessageInterface messageInterface;
  private final HardwareService hardwareService;
  private final ImageService imageService;
  private final LocationService locationService;
  private final IpAddressMessageToIpAddress ipAddressMessageToIpAddress = new IpAddressMessageToIpAddress();
  private final LoginCredentialMessageToLoginCredentialConverter loginCredentialConverter = new LoginCredentialMessageToLoginCredentialConverter();
  private final static String RESOURCE_RETRIEVAL_ERROR = "Could not retrieve detailed information for %s: %s";

  @Inject
  public NodePublisher(MessageInterface messageInterface,
      HardwareService hardwareService, ImageService imageService,
      LocationService locationService) {
    this.messageInterface = messageInterface;
    this.hardwareService = hardwareService;
    this.imageService = imageService;
    this.locationService = locationService;
  }

  @Nullable
  private Image getImage(String id, String userId) throws ResponseException {
    return imageService.getImages(ImageQueryRequest.newBuilder().setUserId(userId).build())
        .getImagesList().stream().filter(
            image -> image.getId().equals(id)).findAny().orElse(null);
  }

  @Nullable
  private HardwareFlavor getHardware(String id, String userId) throws ResponseException {
    return hardwareService.getHardware(HardwareQueryRequest.newBuilder().setUserId(userId).build())
        .getHardwareFlavorsList().stream()
        .filter(hardwareFlavor -> hardwareFlavor.getId().equals(id)).findAny().orElse(null);
  }

  @Nullable
  private Location getLocation(String id, String userId) throws ResponseException {
    return locationService.getLocations(LocationQueryRequest.newBuilder().setUserId(userId).build())
        .getLocationsList().stream().filter(location -> location.getId().equals(id)).findAny()
        .orElse(null);
  }

  public void publish(String imageId, String hardwareId, String locationId,
      VirtualMachine virtualMachine, String userId) throws ResponseException {

    HardwareFlavor hardware = getHardware(hardwareId, userId);
    Image image = getImage(imageId, userId);
    Location location = getLocation(locationId, userId);

    checkState(hardware != null, String.format(RESOURCE_RETRIEVAL_ERROR, "Hardware", hardware));
    checkState(image != null, String.format(RESOURCE_RETRIEVAL_ERROR, "Image", image));
    checkState(location != null, String.format(RESOURCE_RETRIEVAL_ERROR, "Location", location));

    NodeProperties props = NodeProperties.newBuilder()
        .setDisk(hardware.getDisk())
        .setMemory(hardware.getRam()).setNumberOfCores(hardware.getCores())
        .setOperationSystem(image.getOperationSystem())
        .setLocation(location)
        .build();
    messageInterface.publish(NodeEvent.newBuilder()
        .setNode(Node.newBuilder().setUserId(userId)
            .addAllIpAddresses(virtualMachine.privateAddresses().stream()
                .map(s -> ipAddressMessageToIpAddress.applyBack(s,
                    IpAddressType.PRIVATE_IP))
                .collect(
                    Collectors.toList()))
            .addAllIpAddresses(virtualMachine.publicAddresses().stream()
                .map(s -> ipAddressMessageToIpAddress.applyBack(s,
                    IpAddressType.PUBLIC_IP))
                .collect(
                    Collectors.toList()))
            .setLoginCredential(
                loginCredentialConverter.applyBack(virtualMachine.loginCredential().get())).
                setNodeProperties(props).
                setNodeType(NodeType.VM).
                setId(virtualMachine.id()).
                build()).
            setNodeStatus(NodeStatus.CREATED).
            build());

  }
}
