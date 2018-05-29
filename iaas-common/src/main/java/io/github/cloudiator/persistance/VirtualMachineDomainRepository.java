package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import javax.annotation.Nullable;

public class VirtualMachineDomainRepository {

  private final CloudDomainRepository cloudDomainRepository;
  private final LocationDomainRepository locationDomainRepository;
  private final ImageDomainRepository imageDomainRepository;
  private final HardwareDomainRepository hardwareDomainRepository;
  private final LoginCredentialDomainRepository loginCredentialDomainRepository;
  private final IpAddressDomainRepository ipAddressDomainRepository;
  private final VirtualMachineModelRepository virtualMachineModelRepository;

  @Inject
  public VirtualMachineDomainRepository(
      CloudDomainRepository cloudDomainRepository,
      LocationDomainRepository locationDomainRepository,
      ImageDomainRepository imageDomainRepository,
      HardwareDomainRepository hardwareDomainRepository,
      LoginCredentialDomainRepository loginCredentialDomainRepository,
      IpAddressDomainRepository ipAddressDomainRepository,
      VirtualMachineModelRepository virtualMachineModelRepository) {
    this.cloudDomainRepository = cloudDomainRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.imageDomainRepository = imageDomainRepository;
    this.hardwareDomainRepository = hardwareDomainRepository;
    this.loginCredentialDomainRepository = loginCredentialDomainRepository;
    this.ipAddressDomainRepository = ipAddressDomainRepository;
    this.virtualMachineModelRepository = virtualMachineModelRepository;
  }

  public VirtualMachine findById(String id) {
    return null;
  }

  public VirtualMachine findByTenantAndId(String userId, String id) {
    return null;
  }

  public VirtualMachine findAll(String userId) {
    return null;
  }

  public VirtualMachine findAll() {
    return null;
  }

  public void save(VirtualMachine virtualMachine) {

    //retrieve or create the cloud

    //VirtualMachineModel virtualMachineModel = new VirtualMachineModel(virtualMachine.id(),
    //    virtualMachine.providerId(), virtualMachine.name());

  }

  private CloudModel getCloudModel(String id) {
    final String cloudId = IdScopedByClouds.from(id).cloudId();
    return cloudDomainRepository.findModelById(cloudId);
  }

  @Nullable
  private LocationModel getOrCreateLocationModel(VirtualMachine domain) {

    LocationModel locationModel = null;
    if (domain.location().isPresent()) {
      locationModel = locationDomainRepository
          .saveAndGet(domain.location().get());
    }
    return locationModel;
  }

  @Nullable
  private ImageModel getOrCreateImageModel(VirtualMachine domain) {

    ImageModel imageModel = null;
    if (domain.image().isPresent()) {
      imageModel = imageDomainRepository.saveAndGet(domain.image().get());
    }
    return imageModel;

  }

  @Nullable
  private HardwareModel getOrCreateHardwareModel(VirtualMachine domain) {
    HardwareModel hardwareModel = null;
    if (domain.hardware().isPresent()) {
      hardwareModel = hardwareDomainRepository.saveAndGet(domain.hardware().get());
    }
    return hardwareModel;
  }

  @Nullable
  private LoginCredentialModel createLoginCredentialModel(VirtualMachine domain) {
    if (domain.loginCredential().isPresent()) {
      return loginCredentialDomainRepository.saveAndGet(domain.loginCredential().get());
    }
    return null;
  }

  @Nullable
  private IpGroupModel createIpGroupModel(VirtualMachine domain) {
    return ipAddressDomainRepository.saveAndGet(domain.ipAddresses());
  }


  private VirtualMachineModel createModel(VirtualMachine virtualMachine) {

    //retrieve the cloud
    CloudModel cloudModel = getCloudModel(virtualMachine.id());

    checkState(cloudModel != null, String
        .format("Can not save virtualMachine %s as related cloudModel is missing.",
            virtualMachine));

    LocationModel locationModel = getOrCreateLocationModel(virtualMachine);
    HardwareModel hardwareModel = getOrCreateHardwareModel(virtualMachine);
    ImageModel imageModel = getOrCreateImageModel(virtualMachine);
    LoginCredentialModel loginCredentialModel = createLoginCredentialModel(virtualMachine);
    IpGroupModel ipGroupModel = createIpGroupModel(virtualMachine);

    return new VirtualMachineModel(virtualMachine.id(),
        virtualMachine.providerId(), virtualMachine.name(), cloudModel, locationModel,
        loginCredentialModel, imageModel, hardwareModel, ipGroupModel);

  }


}
