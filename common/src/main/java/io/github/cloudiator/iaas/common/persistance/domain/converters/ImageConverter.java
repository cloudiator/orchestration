package io.github.cloudiator.iaas.common.persistance.domain.converters;

import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.ImageBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.iaas.common.persistance.entities.ImageModel;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
public class ImageConverter implements OneWayConverter<ImageModel, Image> {

  @Nullable
  @Override
  public Image apply(@Nullable ImageModel imageModel) {
    if (imageModel == null) {
      return null;
    }
    return ImageBuilder.newBuilder().os(imageModel.operatingSystem())
        .providerId(imageModel.getProviderId()).id(imageModel.getCloudUniqueId())
        .name(imageModel.getName()).build();
  }
}
