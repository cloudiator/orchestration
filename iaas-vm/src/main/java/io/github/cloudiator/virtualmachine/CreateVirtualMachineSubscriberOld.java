package io.github.cloudiator.virtualmachine;

import de.uniulm.omi.cloudiator.sword.domain.KeyPair;
import de.uniulm.omi.cloudiator.sword.domain.TemplateOptionsBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Inject;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineBuilder;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messages.Cloud.CloudQueryResponse;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Vm.CreateVirtualMachineRequestRequest;
import org.cloudiator.messages.Vm.VirtualMachineCreatedResponse;
import org.cloudiator.messages.NodeOuterClass.NodeEvent;
import org.cloudiator.messages.NodeOuterClass.NodeProperties;
import org.cloudiator.messages.NodeOuterClass.NodeStatus;
import org.cloudiator.messages.NodeOuterClass.NodeType;
import org.cloudiator.messages.NodeOuterClass.Node;
import org.cloudiator.messages.entities.CommonEntities.OperatingSystem;
import org.cloudiator.messages.entities.IaasEntities.Cloud;
import org.cloudiator.messages.entities.IaasEntities.IpAddress;
import org.cloudiator.messages.entities.IaasEntities.IpAddressType;
import org.cloudiator.messages.entities.IaasEntities.IpVersion;
import org.cloudiator.messages.entities.IaasEntities.LoginCredential;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachine.Builder;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplate;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachineTemplateBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByCloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;

import io.github.cloudiator.iaas.common.messaging.CloudMessageToCloudConverter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class CreateVirtualMachineSubscriberOld implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CreateVirtualMachineSubscriberOld.class);

  private final MessageInterface messagingService;
  private final CloudService cloudService;
  private volatile Subscription subscription;

  private static final int ILLEGAL_CLOUD_ID = 400;
  private static final int SERVER_ERROR = 500;

  @Inject
  public CreateVirtualMachineSubscriberOld(MessageInterface messageInterface,
      CloudService cloudService) {
    this.messagingService = messageInterface;
    this.cloudService = cloudService;
  }

  @Override
  public void run() {
    subscription = messagingService.subscribe(CreateVirtualMachineRequestRequest.class,
        CreateVirtualMachineRequestRequest.parser(), (requestId, createVirtualMachineRequest) -> {
          VirtualMachineRequest req = createVirtualMachineRequest.getVirtualMachineRequest();
          try {
            DataHolder holder = new DataHolder();
            CreateVirtualMachineSubscriberOld.this.handleRequest(requestId,
                createVirtualMachineRequest.getUserId(), req.getHardware(), req.getImage(),
                req.getLocation(), holder);
            publishCreationEvent(holder);
          } catch (Exception ex) {
            LOGGER.error("exception occurred.", ex);
            CreateVirtualMachineSubscriberOld.this.sendErrorResponse(requestId,
                "exception occurred: " + ex.getMessage(), SERVER_ERROR);
          }
        });
  }

  private final void publishCreationEvent(DataHolder holder) {
    NodeProperties props = NodeProperties.newBuilder().setDisk(holder.diskSize)
        .setMemory(holder.ram).setNumberOfCores(holder.cores).setOperationSystem(holder.os).build();
    messagingService.publish(NodeEvent.newBuilder()
        .setNode(Node.newBuilder().
            addAllIpAddresses(holder.vm.getIpAddressesList())
            .setLoginCredential(holder.vm.getLoginCredential()).
            setNodeProperties(props).
            setNodeType(NodeType.VM).
            setId(holder.vm.getId()).
            build()).
        setNodeStatus(NodeStatus.CREATED).
        build());
  }

  final VirtualMachine handleRequest(final String messageId, String userId, String hardwareId,
      String imageId, String locationId, DataHolder holder) throws ResponseException {
    String cloudId = validateIds(hardwareId, imageId, locationId);
    Cloud cloud = getCloudForUserWithId(userId, cloudId);

    if (cloud != null) {
      LOGGER.info("starting new virtual machine.");
      VirtualMachine vm = createVirtualMachine(cloud, hardwareId, imageId, locationId, holder);
      holder.vm = vm;
      LOGGER.info("virtual machine started. sending response");
      sendSuccessResponse(messageId, vm);
      LOGGER.info("response sent.");
      return vm;
    } else {
      sendErrorResponse(messageId, "cloud identified " + cloudId + " not found.", ILLEGAL_CLOUD_ID);
    }
    return null;
  }

  private VirtualMachine createVirtualMachine(Cloud cloud, String hardwareId, String imageId,
      String locationId, DataHolder holder) {
    MultiCloudService mcs = MultiCloudBuilder.newBuilder().build();
    mcs.cloudRegistry().register(new CloudMessageToCloudConverter().apply(cloud));

    VirtualMachineTemplate vmt = createVmTemplate(hardwareId, imageId, locationId, mcs);
    de.uniulm.omi.cloudiator.sword.domain.VirtualMachine vm =
        mcs.computeService().createVirtualMachine(vmt);
    if (vm.publicAddresses().isEmpty()) {
      vm = this.createPublicIP(mcs.computeService(), vm);
    } else {
      LOGGER.info("public ip address already set.");
    }

    VirtualMachine vmm = createVirtualMachineObject(vm, hardwareId, imageId, locationId, mcs);
    fillHolder(vm, holder);
    holder.vm = vmm;
    return vmm;
  }

  private void fillHolder(de.uniulm.omi.cloudiator.sword.domain.VirtualMachine vm,
      DataHolder holder) {
    holder.cores = vm.hardware().isPresent() ? vm.hardware().get().numberOfCores() : -1;
    holder.ram = vm.hardware().isPresent() ? vm.hardware().get().mbRam() : -1;
    holder.diskSize = vm.hardware().isPresent()
        ? (vm.hardware().get().gbDisk().isPresent() ? vm.hardware().get().gbDisk().get() : -1) : -1;
    de.uniulm.omi.cloudiator.domain.OperatingSystem os =
        vm.image().isPresent() ? vm.image().get().operatingSystem() : null;
    if (os != null) {
      holder.os = OperatingSystem.newBuilder()
          .setOperatingSystemVersion(os.operatingSystemVersion().toString())
          .setOperatingSystemFamily(os.operatingSystemFamily().toString())
          .setOperatingSystemArchitecture(os.operatingSystemArchitecture().toString()).build();
    }
  }

  private VirtualMachine createVirtualMachineObject(
      de.uniulm.omi.cloudiator.sword.domain.VirtualMachine vm, String hardwareId, String imageId,
      String locationId, MultiCloudService mcs) {
    VirtualMachine.Builder builder = VirtualMachine.newBuilder();
    addIpAddresses(builder, vm.publicAddresses(), IpAddressType.PUBLIC_IP);
    addIpAddresses(builder, vm.privateAddresses(), IpAddressType.PRIVATE_IP);
    addLoginCredential(builder, vm.loginCredential().get().username(),
        vm.loginCredential().get().password(), vm.loginCredential().isPresent()
            ? vm.loginCredential().get().privateKey() : Optional.empty());
    builder.setHardware(hardwareId).setImage(imageId).setLocation(locationId);
    return builder.setId(vm.id()).build();
  }

  private VirtualMachineTemplate createVmTemplate(String hardwareId, String imageId,
      String locationId, MultiCloudService mcs) {
    VirtualMachineTemplate vmt = VirtualMachineTemplateBuilder.newBuilder().image(imageId)
        .hardwareFlavor(hardwareId).location(locationId).build();
    final KeyPair keyPairForVM = this.createKeyPairFor(locationId, mcs.computeService());
    if (keyPairForVM != null) {
      vmt =
          VirtualMachineTemplateBuilder.of(vmt)
              .templateOptions(
                  TemplateOptionsBuilder.newBuilder().keyPairName(keyPairForVM.name()).build())
              .build();
    }
    return vmt;
  }

  private void addLoginCredential(Builder builder, Optional<String> username,
      Optional<String> password, Optional<String> privateKey) {

    LoginCredential.Builder creds = LoginCredential.newBuilder();
    if (password.isPresent()) {
      creds.setPassword(password.get());
    }
    if (username.isPresent()) {
      creds.setPassword(password.get());
    }
    if (privateKey.isPresent()) {
      creds.setKeypair(org.cloudiator.messages.entities.IaasEntities.KeyPair.newBuilder()
          .setPrivateKey(privateKey.get()));
    }

    builder.setLoginCredential(creds.build());
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

    de.uniulm.omi.cloudiator.sword.domain.VirtualMachine virtualMachine =
        VirtualMachineBuilder.of(vm).addPublicIpAddress(publicIp).build();

    return virtualMachine;
  }

  private KeyPair createKeyPairFor(String locationId, ComputeService cs) {

    checkNotNull(locationId);
    checkState(!locationId.isEmpty());

    if (!cs.keyPairExtension().isPresent()) {
      return null;
    }

    final KeyPair keyPair = cs.keyPairExtension().get().create(null, locationId);

    checkState(keyPair.privateKey().isPresent(),
        "Expected remote keypair to have a private key, but it has none.");

    return keyPair;
  }

  void terminate() {
    if (subscription != null) {
      subscription.cancel();
    }
  }

  private static class DataHolder {
    int cores;
    long ram;
    float diskSize;
    VirtualMachine vm;
    OperatingSystem os;
  }
}
