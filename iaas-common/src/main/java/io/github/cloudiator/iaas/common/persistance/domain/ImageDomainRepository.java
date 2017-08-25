package io.github.cloudiator.iaas.common.persistance.domain;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.persistance.repositories.ModelRepository;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.iaas.common.persistance.domain.converters.ImageConverter;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.entities.ImageModel;
import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import io.github.cloudiator.iaas.common.persistance.entities.OperatingSystemModel;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.ResourceRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class ImageDomainRepository {

  private final ResourceRepository<ImageModel> imageModelRepository;
  private final ImageConverter imageConverter;
  private final CloudModelRepository cloudModelRepository;
  private final LocationDomainRepository locationDomainRepository;
  private final LocationModelRepository locationModelRepository;
  private final ModelRepository<OperatingSystemModel> operatingSystemModelModelRepository;

  @Inject
  public ImageDomainRepository(
      ResourceRepository<ImageModel> imageModelRepository,
      ImageConverter imageConverter,
      CloudModelRepository cloudModelRepository,
      LocationDomainRepository locationDomainRepository,
      LocationModelRepository locationModelRepository,
      ModelRepository<OperatingSystemModel> operatingSystemModelModelRepository) {
    this.imageModelRepository = imageModelRepository;
    this.imageConverter = imageConverter;
    this.cloudModelRepository = cloudModelRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.locationModelRepository = locationModelRepository;
    this.operatingSystemModelModelRepository = operatingSystemModelModelRepository;
  }

  public Image findById(String id) {
    return imageConverter.apply(imageModelRepository.findByCloudUniqueId(id));
  }

  public Image findByTenantAndId(String userId, String imageId) {
    return imageConverter
        .apply(imageModelRepository.findByCloudUniqueIdAndTenant(userId, imageId));
  }

  public List<Image> findByTenantAndCloud(String tenantId, String cloudId) {
    return imageModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(imageConverter).collect(Collectors.toList());
  }

  public void save(Image image) {

    //get corresponding cloudModel
    final String cloudId = IdScopedByClouds.from(image.id()).cloudId();
    final CloudModel cloudModel = cloudModelRepository
        .getByCloudId(cloudId);
    checkState(cloudModel != null, String
        .format("Can not save image %s as related cloudModel with id %s is missing.",
            image, cloudId));

    LocationModel locationModel = null;
    if (image.location().isPresent()) {
      locationDomainRepository.save(image.location().get());
      locationModel = locationModelRepository
          .findByCloudUniqueId(image.location().get().id());
      checkState(locationModel != null);
    }

    //generate operating system
    OperatingSystemModel operatingSystemModel = new OperatingSystemModel(
        image.operatingSystem().operatingSystemArchitecture(),
        image.operatingSystem().operatingSystemFamily(),
        String.valueOf(image.operatingSystem().operatingSystemVersion()));
    operatingSystemModelModelRepository.save(operatingSystemModel);

    ImageModel imageModel = imageModelRepository
        .findByCloudUniqueId(image.id());
    //todo handle update case
    if (imageModel == null) {
      imageModel = new ImageModel(image.id(), image.providerId(), image.name(), cloudModel,
          locationModel, null, null, operatingSystemModel);
    }
    imageModelRepository.save(imageModel);
  }

  public void delete(Image image) {
    final ImageModel byCloudUniqueId = imageModelRepository.findByCloudUniqueId(image.id());
    imageModelRepository.delete(byCloudUniqueId);
  }

  public List<Image> findAll() {
    return imageModelRepository.findAll().stream().map(imageConverter).collect(Collectors.toList());
  }

  public List<Image> findAll(String user) {
    List<ImageModel> imageModels = imageModelRepository.findByTenant(user);
    return imageModels.stream().map(imageConverter)
        .collect(Collectors.toList());
  }
}
