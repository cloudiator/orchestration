package io.github.cloudiator.iaas.common.messaging.domain;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import io.github.cloudiator.iaas.common.messaging.converters.ImageMessageToImageConverter;
import io.github.cloudiator.iaas.common.util.CollectorsUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ImageService;

public class ImageMessageRepository implements MessageRepository<Image> {

  private final ImageService imageService;
  private final ImageMessageToImageConverter converter = new ImageMessageToImageConverter();
  private final static String RESPONSE_ERROR = "Could not retrieve image object(s) due to error %s";

  @Inject
  public ImageMessageRepository(
      ImageService imageService) {
    this.imageService = imageService;
  }

  @Override
  public Image getById(String userId, String id) {
    try {
      return imageService
          .getImages(ImageQueryRequest.newBuilder().setImageId(id).setUserId(userId).build())
          .getImagesList().stream().map(converter).collect(CollectorsUtil.singletonCollector());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }

  @Override
  public List<Image> getAll(String userId) {
    try {
      return imageService
          .getImages(ImageQueryRequest.newBuilder().setUserId(userId).build())
          .getImagesList().stream().map(converter).collect(
              Collectors.toList());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }
}
