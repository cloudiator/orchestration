package io.github.cloudiator.iaas.common.persistance.messaging.converters;

import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.ImageBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Image.Builder;

/**
 * Created by daniel on 07.06.17.
 */
public class ImageMessageToImageConverter implements TwoWayConverter<IaasEntities.Image, Image> {

  private LocationMessageToLocationConverter locationMessageToLocationConverter = new LocationMessageToLocationConverter();
  private OperatingSystemConverter operatingSystemConverter = new OperatingSystemConverter();

  @Override
  public IaasEntities.Image applyBack(Image image) {
    Builder builder = IaasEntities.Image.newBuilder().setId(image.id())
        .setProviderId(image.providerId())
        .setName(image.name())
        .setOperationSystem(operatingSystemConverter.applyBack(image.operatingSystem()));
    if (image.location().isPresent()) {
      builder.setLocation(locationMessageToLocationConverter.applyBack(image.location().get()));
    }
    return builder.build();
  }

  @Override
  public Image apply(IaasEntities.Image image) {
    return ImageBuilder.newBuilder().id(image.getId()).providerId(image.getProviderId())
        .name(image.getName()).os(operatingSystemConverter.apply(image.getOperationSystem()))
        .location(locationMessageToLocationConverter.apply(image.getLocation())).build();
  }
}
