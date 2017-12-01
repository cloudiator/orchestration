package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import io.github.cloudiator.iaas.common.messaging.converters.ImageMessageToImageConverter;
import io.github.cloudiator.iaas.common.persistance.domain.ImageDomainRepository;
import java.util.stream.Collectors;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messages.Image.ImageQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

;

/**
 * Created by daniel on 01.06.17.
 */
public class ImageQuerySubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final ImageDomainRepository imageDomainRepository;
  private final ImageMessageToImageConverter imageConverter;
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageQuerySubscriber.class);

  @Inject
  public ImageQuerySubscriber(MessageInterface messageInterface,
      ImageDomainRepository imageDomainRepository,
      ImageMessageToImageConverter imageConverter) {
    this.messageInterface = messageInterface;
    this.imageDomainRepository = imageDomainRepository;
    this.imageConverter = imageConverter;
  }

  @Override
  public void run() {
    Subscription subscription = messageInterface
        .subscribe(ImageQueryRequest.class, ImageQueryRequest.parser(),
            (requestId, imageQueryRequest) -> {

              try {
                decideAndReply(requestId, imageQueryRequest);
              } catch (Exception e) {
                LOGGER.error(String
                    .format("Caught exception %s during execution of %s", e.getMessage(), this), e);
              }
            });
  }

  private void decideAndReply(String requestId, ImageQueryRequest request) {
    if (request.getUserId().isEmpty()) {
      replyErrorNoUserId(requestId);
      return;
    }
    if (!request.getImageId().isEmpty()) {
      replyForUserIdAndImageId(requestId, request.getUserId(), request.getImageId());
      return;
    }
    if (!request.getCloudId().isEmpty()) {
      replyForUserIdAndCloudId(requestId, request.getUserId(), request.getCloudId());
      return;
    }
    replyForUserId(requestId, request.getUserId());
  }


  private void replyErrorNoUserId(String requestId) {
    messageInterface.reply(ImageQueryResponse.class, requestId,
        Error.newBuilder().setCode(500).setMessage("Request does not contain userId.")
            .build());
  }


  private void replyForUserIdAndImageId(String requestId, String userId, String imageId) {
    final Image image = imageDomainRepository
        .findByTenantAndId(userId, imageId);
    if (image == null) {
      messageInterface.reply(ImageQueryResponse.class, requestId,
          Error.newBuilder().setCode(404)
              .setMessage(String.format("Image with id %s was not found.", imageId))
              .build());
    } else {
      ImageQueryResponse imageQueryResponse = ImageQueryResponse.newBuilder()
          .addImages(imageConverter.applyBack(image)).build();
      messageInterface.reply(requestId, imageQueryResponse);
    }

  }

  private void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    ImageQueryResponse imageQueryResponse = ImageQueryResponse.newBuilder()
        .addAllImages(
            imageDomainRepository.findByTenantAndCloud(userId, cloudId).stream().map(
                imageConverter::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, imageQueryResponse);
  }

  private void replyForUserId(String requestId, String userId) {
    ImageQueryResponse imageQueryResponse = ImageQueryResponse.newBuilder()
        .addAllImages(imageDomainRepository.findAll(userId).stream().map(
            imageConverter::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, imageQueryResponse);
  }
}
