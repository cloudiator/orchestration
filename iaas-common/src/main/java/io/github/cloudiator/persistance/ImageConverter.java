package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.ImageBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
class ImageConverter implements OneWayConverter<ImageModel, Image> {

  private final LocationConverter locationConverter = new LocationConverter();

  @Nullable
  @Override
  public Image apply(@Nullable ImageModel imageModel) {
    if (imageModel == null) {
      return null;
    }
    return ImageBuilder.newBuilder().os(imageModel.operatingSystem())
        .location(locationConverter.apply(imageModel.getLocationModel()))
        .providerId(imageModel.getProviderId()).id(imageModel.getCloudUniqueId())
        .name(imageModel.getName()).build();
  }
}
