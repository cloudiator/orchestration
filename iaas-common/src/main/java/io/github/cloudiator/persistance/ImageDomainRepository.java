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
  private final OperatingSystemDomainRepository operatingSystemDomainRepository;

  @Inject
  public ImageDomainRepository(
      ResourceRepository<ImageModel> imageModelRepository,
      LocationDomainRepository locationDomainRepository,
      CloudDomainRepository cloudDomainRepository,
      OperatingSystemDomainRepository operatingSystemDomainRepository) {
    this.imageModelRepository = imageModelRepository;
    this.locationDomainRepository = locationDomainRepository;
    this.cloudDomainRepository = cloudDomainRepository;
    this.operatingSystemDomainRepository = operatingSystemDomainRepository;
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
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  ImageModel saveAndGet(Image domain) {
    checkNotNull(domain, "domain is null");

    ImageModel model = imageModelRepository.findByCloudUniqueId(domain.id());
    if (model == null) {
      model = createModel(domain);
    } else {
      updateModel(domain, model);
    }
    imageModelRepository.save(model);
    return model;
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

  private ImageModel createModel(Image domain) {
    final CloudModel cloudModel = getCloudModel(domain.id());

    checkState(cloudModel != null, String
        .format("Can not save image %s as related cloudModel is missing.",
            domain));

    LocationModel locationModel = getOrCreateLocationModel(domain);

    OperatingSystemModel operatingSystemModel = operatingSystemDomainRepository
        .saveAndGet(domain.operatingSystem());

    ImageModel imageModel = new ImageModel(domain.id(), domain.providerId(), domain.name(),
        cloudModel,
        locationModel, null, null, operatingSystemModel);

    imageModelRepository.save(imageModel);

    return imageModel;
  }

  private void updateModel(Image domain, ImageModel model) {

    //currently we only update the operating system.
    //todo throw exception if something else is changed?

    operatingSystemDomainRepository.update(domain.operatingSystem(), model.operatingSystem());
    imageModelRepository.save(model);
  }

  public Collection<Image> findAll(@Nullable String userId) {
    checkNotNull(userId, "userId is null");
    return imageModelRepository.findByTenant(userId).stream().map(IMAGE_CONVERTER)
        .collect(Collectors.toList());
  }
}
