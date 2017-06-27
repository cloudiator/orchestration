package io.github.cloudiator.virtualmachine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Inject;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import io.github.cloudiator.workflow.Exchange;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messages.Cloud.CloudQueryResponse;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestRequest;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messages.entities.IaasEntities.Cloud;
import org.cloudiator.messages.entities.IaasEntities.IpAddress;
import org.cloudiator.messages.entities.IaasEntities.IpAddressType;
import org.cloudiator.messages.entities.IaasEntities.IpVersion;
import org.cloudiator.messages.entities.IaasEntities.KeyPair;
import org.cloudiator.messages.entities.IaasEntities.LoginCredential;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine.Builder;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.services.CloudService;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplateBuilder;

import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByCloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;

import io.github.cloudiator.iaas.common.messaging.CloudMessageToCloudConverter;

import static com.google.common.base.Preconditions.checkState;

public class CreateVirtualMachineSubscriber implements Runnable {

  private final MessageInterface messagingService;
  private final CloudService cloudService;

  private static final int ILLEGAL_CLOUD_ID = 400;
  private static final int SERVER_ERROR = 500;

  @Inject
  public CreateVirtualMachineSubscriber(MessageInterface messageInterface,
      CloudService cloudService) {
    this.messagingService = messageInterface;
    this.cloudService = cloudService;
  }

  @Override
  public void run() {
    @SuppressWarnings("unused")
    Subscription subscription = messagingService.subscribe(CreateVirtualMachineRequestRequest.class,
        CreateVirtualMachineRequestRequest.parser(), (requestId, createVirtualMachineRequest) -> {
          VirtualMachineRequest req = createVirtualMachineRequest.getVirtualMachineRequest();
          try {
            CreateVirtualMachineSubscriber.this.handleRequest(requestId,
                createVirtualMachineRequest.getUserId(), req.getHardware(), req.getImage(),
                req.getLocation());
          } catch (Exception ex) {
            CreateVirtualMachineSubscriber.this.sendErrorResponse(requestId,
                "exception occurred: " + ex.getMessage(), SERVER_ERROR);
          }
        });
  }

  final void handleRequest(final String messageId, String userId, String hardwareId, String imageId,
      String locationId) throws ResponseException {
    String cloudId = validateIds(hardwareId, imageId, locationId);
    Cloud cloud = getCloudForUserWithId(userId, cloudId);

    if (cloud != null) {
      VirtualMachine vm = createVirtualMachine(cloud, hardwareId, imageId, locationId);
      sendSuccessResponse(messageId, vm);
    } else {
      sendErrorResponse(messageId, "cloud identified " + cloudId + " not found.", ILLEGAL_CLOUD_ID);
    }
  }

  private VirtualMachine createVirtualMachine(Cloud cloud, String hardwareId, String imageId,
      String locationId) {
    MultiCloudService mcs = MultiCloudBuilder.newBuilder().build();
    mcs.cloudRegistry().register(new CloudMessageToCloudConverter().apply(cloud));
    VirtualMachineTemplate vmt = VirtualMachineTemplateBuilder.newBuilder().image(imageId)
        .hardwareFlavor(hardwareId).location(locationId).build();
    de.uniulm.omi.cloudiator.sword.domain.VirtualMachine vm =
        mcs.computeService().createVirtualMachine(vmt);

    if (vm.publicAddresses().isEmpty()) {
      vm = this.createPublicIP(mcs.computeService(), vm);
    }

    VirtualMachine.Builder builder = VirtualMachine.newBuilder();
    addIpAddresses(builder, vm.privateAddresses(), IpAddressType.PRIVATE_IP);
    addIpAddresses(builder, vm.privateAddresses(), IpAddressType.PUBLIC_IP);
    addLoginCredential(builder, vm.loginCredential().get().username(),
        vm.loginCredential().get().password(), vm.loginCredential().get().privateKey());
    builder.setHardware(hardwareId).setImage(imageId).setLocation(locationId);

    return builder.build();
  }

  private void addLoginCredential(Builder builder,
      Optional<String> username, Optional<String> password, Optional<String> privateKey) {
    builder.setLoginCredential(
        LoginCredential.newBuilder().setPassword(password.get()).setUsername(username.get())
            .setKeypair(KeyPair.newBuilder().setPrivateKey(privateKey.get())));
  }

  private void addIpAddresses(VirtualMachine.Builder builder, Set<String> ips, IpAddressType type) {
    for (String ip : ips) {
      IpAddress address =
          IpAddress.newBuilder().setIp(ip).setType(type).setVersion(getIpVersion(ip)).build();
      builder.addIpAddresses(address);
    }
  }

  private IpVersion getIpVersion(String ip) {
    try {
      InetAddress address = java.net.InetAddress.getByName(ip);
      if (address instanceof java.net.Inet4Address) {
        return IpVersion.V4;
      }
      if (address instanceof java.net.Inet6Address) {
        return IpVersion.v6;
      }
      throw new IllegalArgumentException(ip);
    } catch (UnknownHostException uhe) {
      throw new IllegalArgumentException(ip, uhe);
    }
  }

  final void sendSuccessResponse(String messageId, VirtualMachine vm) {
    messagingService.reply(messageId,
        VirtualMachineCreatedResponse.newBuilder().setVirtualMachine(vm).build());
  }

  final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messagingService.reply(VirtualMachineCreatedResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }

  private final Cloud getCloudForUserWithId(String userId, final String cloudId)
      throws ResponseException {
    Predicate<Cloud> predicate = new Predicate<Cloud>() {
      @Override
      public boolean test(Cloud cloud) {
        return cloudId.equals(cloud.getId());
      }
    };

    CloudQueryResponse resp =
        cloudService.getClouds(CloudQueryRequest.newBuilder().setUserId(userId).build());
    return resp.getCloudsList().stream().filter(predicate).findFirst().orElse(null);
  }

  private final String validateIds(String hardwareId, String imageId, String locationId) {
    IdScopedByCloud id = IdScopedByClouds.from(hardwareId);
    if (hasSameCloudId(id, imageId) && hasSameCloudId(id, locationId)) {
      return id.cloudId();
    } else {
      throw new IllegalArgumentException("ids do not belong to same cloud or cloud not set.");
    }
  }

  private final boolean hasSameCloudId(IdScopedByCloud id, String otherId) {
    assert id != null : "id cannot be null";
    assert id.cloudId() != null : "cloud id not set";
    assert otherId != null : "otherId must not be null";

    IdScopedByCloud otherScoped = IdScopedByClouds.from(otherId);
    assert otherScoped != null : "no cloud id found";

    return id.cloudId().equals(otherScoped.cloudId());
  }

  private final de.uniulm.omi.cloudiator.sword.domain.VirtualMachine createPublicIP(
      ComputeService cs, de.uniulm.omi.cloudiator.sword.domain.VirtualMachine vm) {
    checkState(cs.publicIpExtension().isPresent());

    String publicIp = cs.publicIpExtension().get().addPublicIp(vm.id());

    de.uniulm.omi.cloudiator.sword.domain.VirtualMachine virtualMachine = VirtualMachineBuilder
        .of(vm).addPublicIpAddress(publicIp).build();

    return virtualMachine;
  }

  private final void createKeyPair() {

  }
}
