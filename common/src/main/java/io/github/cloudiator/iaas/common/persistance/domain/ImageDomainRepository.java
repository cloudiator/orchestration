package io.github.cloudiator.iaas.common.persistance.domain;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.iaas.common.persistance.domain.converters.ImageConverter;
import io.github.cloudiator.iaas.common.persistance.entities.Cloud;
import io.github.cloudiator.iaas.common.persistance.entities.ImageModel;
import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import io.github.cloudiator.iaas.common.persistance.entities.OperatingSystemModel;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.ModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.ResourceRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class ImageDomainRepository implements DomainRepository<Image> {

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

  @Override
  public Image findById(String id) {
    return imageConverter.apply(imageModelRepository.findByCloudUniqueId(id));
  }

  @Override
  public void save(Image image) {

    //get corresponding cloud
    final String cloudId = IdScopedByClouds.from(image.id()).cloudId();
    final Cloud cloud = cloudModelRepository
        .getByCloudId(cloudId);
    checkState(cloud != null, String
        .format("Can not save image %s as related cloud with id %s is missing.",
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
      imageModel = new ImageModel(image.id(), image.providerId(), image.name(), cloud,
          locationModel, null, null, operatingSystemModel);
    }
    imageModelRepository.save(imageModel);
  }

  @Override
  public void delete(Image image) {
    final ImageModel byCloudUniqueId = imageModelRepository.findByCloudUniqueId(image.id());
    imageModelRepository.delete(byCloudUniqueId);
  }

  @Override
  public List<Image> findAll() {
    return imageModelRepository.findAll().stream().map(imageConverter).collect(Collectors.toList());
  }
}
