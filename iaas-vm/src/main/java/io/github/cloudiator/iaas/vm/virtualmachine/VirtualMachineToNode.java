package io.github.cloudiator.iaas.vm.virtualmachine;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.cloudiator.iaas.common.domain.Node;
import io.github.cloudiator.iaas.common.domain.NodeBuilder;
import io.github.cloudiator.iaas.common.domain.NodeProperties;
import io.github.cloudiator.iaas.common.domain.NodePropertiesBuilder;
import io.github.cloudiator.iaas.common.domain.NodeType;
import io.github.cloudiator.iaas.common.messaging.HardwareMessageToHardwareConverter;
import io.github.cloudiator.iaas.common.messaging.ImageMessageToImageConverter;
import io.github.cloudiator.iaas.common.messaging.LocationMessageToLocationConverter;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.HardwareService;
import org.cloudiator.messaging.services.ImageService;
import org.cloudiator.messaging.services.LocationService;

public class VirtualMachineToNode {

  private final HardwareService hardwareService;
  private final ImageService imageService;
  private final LocationService locationService;
  private final LocationMessageToLocationConverter locationConverter = new LocationMessageToLocationConverter();
  private final HardwareMessageToHardwareConverter hardwareConverter = new HardwareMessageToHardwareConverter();
  private final ImageMessageToImageConverter imageConverter = new ImageMessageToImageConverter();

  @Inject
  public VirtualMachineToNode(HardwareService hardwareService,
      ImageService imageService, LocationService locationService) {
    this.hardwareService = hardwareService;
    this.imageService = imageService;
    this.locationService = locationService;
  }

  @Nullable
  private IaasEntities.Image getImage(String id, String userId) throws ResponseException {
    return imageService.getImages(ImageQueryRequest.newBuilder().setUserId(userId).build())
        .getImagesList().stream().filter(
            image -> image.getId().equals(id)).findAny().orElse(null);
  }

  @Nullable
  private IaasEntities.HardwareFlavor getHardware(String id, String userId)
      throws ResponseException {
    return hardwareService.getHardware(HardwareQueryRequest.newBuilder().setUserId(userId).build())
        .getHardwareFlavorsList().stream()
        .filter(hardwareFlavor -> hardwareFlavor.getId().equals(id)).findAny().orElse(null);
  }

  @Nullable
  private IaasEntities.Location location(String id, String userId) throws ResponseException {
    return locationService.getLocations(LocationQueryRequest.newBuilder().setUserId(userId).build())
        .getLocationsList().stream().filter(location -> location.getId().equals(id)).findAny()
        .orElse(null);
  }

  public Node apply(VirtualMachine virtualMachine, String userId) throws ResponseException {

    final HardwareFlavor hardwareFlavor = hardwareConverter
        .apply(getHardware(virtualMachine.hardware().get().id(), userId));
    final Image image = imageConverter.apply(getImage(virtualMachine.image().get().id(), userId));
    final Location location = locationConverter
        .apply(location(virtualMachine.location().get().id(), userId));

    NodeProperties nodeProperties = NodePropertiesBuilder.of(hardwareFlavor, image, location)
        .build();
    return NodeBuilder.newBuilder().nodeType(NodeType.VM).ipAddresses(virtualMachine.ipAddresses())
        .loginCredential(virtualMachine.loginCredential().orElse(null))
        .nodeProperties(nodeProperties).id(virtualMachine.id()).build();
  }
}
