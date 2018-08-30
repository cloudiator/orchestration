package io.github.cloudiator.messaging;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ImageService;

public class ImageMessageRepository implements MessageRepository<Image> {

  private final static String RESPONSE_ERROR = "Could not retrieve image object(s) due to error %s";
  private final ImageService imageService;
  private static final ImageMessageToImageConverter CONVERTER = ImageMessageToImageConverter.INSTANCE;

  @Inject
  public ImageMessageRepository(
      ImageService imageService) {
    this.imageService = imageService;
  }

  @Override
  @Nullable
  public Image getById(String userId, String id) {

    try {
      final List<Image> collect = imageService
          .getImages(ImageQueryRequest.newBuilder().setImageId(id).setUserId(userId).build())
          .getImagesList().stream().map(CONVERTER).collect(Collectors.toList());

      checkState(collect.size() <= 1, "Expected unique result.");

      if (collect.isEmpty()) {
        return null;
      }
      return collect.get(0);


    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }

  @Override
  public List<Image> getAll(String userId) {
    try {
      return imageService
          .getImages(ImageQueryRequest.newBuilder().setUserId(userId).build())
          .getImagesList().stream().map(CONVERTER).collect(
              Collectors.toList());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }
}
