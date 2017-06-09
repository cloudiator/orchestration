package io.github.cloudiator.iaas.common.persistance.domain;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.iaas.common.persistance.domain.converters.HardwareConverter;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareModel;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareOffer;
import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.HardwareOfferRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.ResourceRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class HardwareDomainRepository implements DomainRepository<HardwareFlavor> {

  private final ResourceRepository<HardwareModel> hardwareModelRepository;
  private final HardwareConverter hardwareConverter = new HardwareConverter();
  private final CloudModelRepository cloudModelRepository;
  private final LocationDomainRepository locationDomainRepository;
  private final HardwareOfferRepository hardwareOfferRepository;
  private final LocationModelRepository locationModelRepository;

  @Inject
  public HardwareDomainRepository(
      ResourceRepository<HardwareModel> hardwareModelRepository,
      CloudModelRepository cloudModelRepository,
      LocationDomainRepository locationDomainRepository,
      HardwareOfferRepository hardwareOfferRepository,
      LocationModelRepository locationModelRepository) {
    this.hardwareModelRepository = hardwareModelRepository;
    this.cloudModelRepository = cloudModelRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.hardwareOfferRepository = hardwareOfferRepository;
    this.locationModelRepository = locationModelRepository;
  }


  @Override
  public HardwareFlavor findById(String id) {
    return hardwareConverter.apply(hardwareModelRepository.findByCloudUniqueId(id));
  }

  @Override
  public void save(HardwareFlavor hardwareFlavor) {
    //get corresponding cloudModel
    final String cloudId = IdScopedByClouds.from(hardwareFlavor.id()).cloudId();
    final CloudModel cloudModel = cloudModelRepository
        .getByCloudId(cloudId);
    checkState(cloudModel != null, String
        .format("Can not save hardwareFlavor %s as related cloudModel with id %s is missing.",
            hardwareFlavor, cloudId));

    LocationModel locationModel = null;
    if (hardwareFlavor.location().isPresent()) {
      locationDomainRepository.save(hardwareFlavor.location().get());
      locationModel = locationModelRepository
          .findByCloudUniqueId(hardwareFlavor.location().get().id());
      checkState(locationModel != null);
    }

    //generate hardware offer
    HardwareOffer hardwareOffer = hardwareOfferRepository
        .findByCpuRamDisk(hardwareFlavor.numberOfCores(), hardwareFlavor.mbRam(),
            hardwareFlavor.gbDisk().orElse(null));
    if (hardwareOffer == null) {
      hardwareOffer = new HardwareOffer(hardwareFlavor.numberOfCores(), hardwareFlavor.mbRam(),
          hardwareFlavor.gbDisk().orElse(null));
    }
    hardwareOfferRepository.save(hardwareOffer);

    HardwareModel hardwareModelEntity = hardwareModelRepository
        .findByCloudUniqueId(hardwareFlavor.id());
    //todo handle update case
    if (hardwareModelEntity == null) {
      hardwareModelEntity = new HardwareModel(hardwareFlavor.id(), hardwareFlavor.providerId(),
          hardwareFlavor.name(), cloudModel, locationModel, hardwareOffer);
    }
    hardwareModelRepository.save(hardwareModelEntity);
  }

  @Override
  public void delete(HardwareFlavor hardwareFlavor) {
    final HardwareModel byCloudUniqueId = this.hardwareModelRepository
        .findByCloudUniqueId(hardwareFlavor.id());
    this.hardwareModelRepository.delete(byCloudUniqueId);
  }

  @Override
  public List<HardwareFlavor> findAll() {
    return hardwareModelRepository.findAll().stream().map(
        hardwareConverter).collect(Collectors.toList());
  }

  public List<HardwareFlavor> findAll(String user) {
    return hardwareModelRepository.findByTenant(user).stream().map(
        hardwareConverter).collect(Collectors.toList());
  }

}
