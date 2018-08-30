package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.ImageBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Image.Builder;

/**
 * Created by daniel on 07.06.17.
 */
public class ImageMessageToImageConverter implements TwoWayConverter<IaasEntities.Image, Image> {

  public static final ImageMessageToImageConverter INSTANCE = new ImageMessageToImageConverter();

  private ImageMessageToImageConverter() {
  }


  private static final LocationMessageToLocationConverter LOCATION_CONVERTER = LocationMessageToLocationConverter.INSTANCE;
  private OperatingSystemConverter operatingSystemConverter = new OperatingSystemConverter();

  @Override
  public IaasEntities.Image applyBack(Image image) {
    Builder builder = IaasEntities.Image.newBuilder().setId(image.id())
        .setProviderId(image.providerId())
        .setName(image.name())
        .setOperationSystem(operatingSystemConverter.applyBack(image.operatingSystem()));
    if (image.location().isPresent()) {
      builder.setLocation(LOCATION_CONVERTER.applyBack(image.location().get()));
    }
    return builder.build();
  }

  @Override
  public Image apply(IaasEntities.Image image) {
    return ImageBuilder.newBuilder().id(image.getId()).providerId(image.getProviderId())
        .name(image.getName()).os(operatingSystemConverter.apply(image.getOperationSystem()))
        .location(LOCATION_CONVERTER.apply(image.getLocation())).build();
  }
}
