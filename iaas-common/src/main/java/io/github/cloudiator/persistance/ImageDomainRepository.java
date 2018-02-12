package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
public class ImageDomainRepository {

  private final ResourceRepository<ImageModel> imageModelRepository;
  private static final ImageConverter IMAGE_CONVERTER = new ImageConverter();
  private final LocationDomainRepository locationDomainRepository;
  private final CloudDomainRepository cloudDomainRepository;

  @Inject
  public ImageDomainRepository(
      ResourceRepository<ImageModel> imageModelRepository,
      LocationDomainRepository locationDomainRepository,
      CloudDomainRepository cloudDomainRepository) {
    this.imageModelRepository = imageModelRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.cloudDomainRepository = cloudDomainRepository;
  }


  public Image findById(String id) {
    return IMAGE_CONVERTER.apply(imageModelRepository.findByCloudUniqueId(id));
  }

  public Image findByTenantAndId(String userId, String imageId) {
    return IMAGE_CONVERTER
        .apply(imageModelRepository.findByCloudUniqueIdAndTenant(userId, imageId));
  }

  public List<Image> findByTenantAndCloud(String tenantId, String cloudId) {
    return imageModelRepository.findByTenantAndCloud(tenantId, cloudId).stream()
        .map(IMAGE_CONVERTER).collect(Collectors.toList());
  }

  public void save(Image domain) {

  }

  ImageModel saveAndGet(Image domain) {

  }

  private CloudModel getCloudModel(String id) {
    final String cloudId = IdScopedByClouds.from(id).cloudId();
    return cloudDomainRepository.findModelById(cloudId);
  }

  @Nullable
  private LocationModel getOrCreateLocationModel(Image domain) {

    LocationModel locationModel = null;
    if (domain.location().isPresent()) {
      locationModel = locationDomainRepository
          .saveAndGet(domain.location().get());
    }
    return locationModel;

  }

  public void tmp(Image image) {

    //get corresponding cloudModel
    final String cloudId = IdScopedByClouds.from(image.id()).cloudId();
    final CloudModel cloudModel = cloudModelRepository
        .getByCloudId(cloudId);

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


  private ImageModel createModel(Image domain) {
    final CloudModel cloudModel = getCloudModel(domain.id());

    checkState(cloudModel != null, String
        .format("Can not save image %s as related cloudModel is missing.",
            domain));

  }

  private ImageModel updateModel(Image domain, ImageModel model) {
    return null;
  }

  @Override
  public Collection<Image> findAll(@Nullable String userId) {
    checkNotNull(userId, "userId is null");
    return imageModelRepository.findByTenant(userId).stream().map(IMAGE_CONVERTER)
        .collect(Collectors.toList());
  }
}
