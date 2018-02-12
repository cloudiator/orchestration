package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
public class HardwareDomainRepository {

  private final ResourceRepository<HardwareModel> hardwareModelRepository;
  private static final HardwareConverter HARDWARE_CONVERTER = new HardwareConverter();
  private final CloudDomainRepository cloudDomainRepository;
  private final LocationDomainRepository locationDomainRepository;
  private final HardwareOfferModelRepository hardwareOfferModelRepository;
  private final LocationModelRepository locationModelRepository;

  @Inject
  public HardwareDomainRepository(
      ResourceRepository<HardwareModel> hardwareModelRepository,
      CloudDomainRepository cloudDomainRepository,
      LocationDomainRepository locationDomainRepository,
      HardwareOfferModelRepository hardwareOfferModelRepository,
      LocationModelRepository locationModelRepository) {
    this.hardwareModelRepository = hardwareModelRepository;
    this.cloudDomainRepository = cloudDomainRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.hardwareOfferModelRepository = hardwareOfferModelRepository;
    this.locationModelRepository = locationModelRepository;
  }


  public HardwareFlavor findById(String id) {
    return HARDWARE_CONVERTER.apply(hardwareModelRepository.findByCloudUniqueId(id));
  }

  public HardwareFlavor findByTenantAndId(String userId, String hardwareId) {
    return HARDWARE_CONVERTER
        .apply(hardwareModelRepository.findByCloudUniqueIdAndTenant(userId, hardwareId));
  }

  public List<HardwareFlavor> findByTenantAndCloud(String tenantId, String cloudId) {
    return hardwareModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(HARDWARE_CONVERTER).collect(Collectors.toList());
  }

  public void save(HardwareFlavor domain) {
    saveAndGet(domain);
  }

  HardwareModel saveAndGet(HardwareFlavor domain) {

    HardwareModel hardwareModel = hardwareModelRepository.findByCloudUniqueId(domain.id());

    if (hardwareModel == null) {
      hardwareModel = createModel(domain);
    } else {
      updateModel(domain, hardwareModel);
    }

    hardwareModelRepository.save(hardwareModel);
    return hardwareModel;
  }

  private HardwareModel createModel(HardwareFlavor domain) {

    //get corresponding cloudModel
    final CloudModel cloudModel = getCloudModel(domain.id());
    checkState(cloudModel != null, String
        .format("Can not save hardwareFlavor %s as related cloudModel is missing.",
            domain));

    final HardwareOfferModel hardwareOfferModel = getOrCreateHardwareOffer(domain);

    final LocationModel locationModel = getOrCreateLocationModel(domain);

    return new HardwareModel(domain.id(), domain.providerId(),
        domain.name(), cloudModel, locationModel, hardwareOfferModel);
  }

  private void updateModel(HardwareFlavor domain, HardwareModel model) {
    //we only allow update of the hardware offer object
    //todo throw exception if other elements change?
    model.setHardwareOfferModel(getOrCreateHardwareOffer(domain));
  }

  private CloudModel getCloudModel(String id) {
    final String cloudId = IdScopedByClouds.from(id).cloudId();
    return cloudDomainRepository.findModelById(cloudId);
  }

  private HardwareOfferModel getOrCreateHardwareOffer(HardwareFlavor domain) {
    //generate hardware offer
    HardwareOfferModel hardwareOfferModel = hardwareOfferModelRepository
        .findByCpuRamDisk(domain.numberOfCores(), domain.mbRam(),
            domain.gbDisk().orElse(null));
    if (hardwareOfferModel == null) {
      hardwareOfferModel = new HardwareOfferModel(domain.numberOfCores(),
          domain.mbRam(),
          domain.gbDisk().orElse(null));
    }
    hardwareOfferModelRepository.save(hardwareOfferModel);
    return hardwareOfferModel;
  }


  @Nullable
  private LocationModel getOrCreateLocationModel(HardwareFlavor domain) {

    LocationModel locationModel = null;
    if (domain.location().isPresent()) {
      locationModel = locationDomainRepository
          .saveAndGet(domain.location().get());
    }
    return locationModel;

  }

  public void delete(HardwareFlavor hardwareFlavor) {
    final HardwareModel byCloudUniqueId = this.hardwareModelRepository
        .findByCloudUniqueId(hardwareFlavor.id());
    this.hardwareModelRepository.delete(byCloudUniqueId);
  }

  public List<HardwareFlavor> findAll() {
    return hardwareModelRepository.findAll().stream().map(
        HARDWARE_CONVERTER).collect(Collectors.toList());
  }

  public List<HardwareFlavor> findAll(String user) {
    return hardwareModelRepository.findByTenant(user).stream().map(
        HARDWARE_CONVERTER).collect(Collectors.toList());
  }

}
