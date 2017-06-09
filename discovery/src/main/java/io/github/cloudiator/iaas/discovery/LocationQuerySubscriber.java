package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import io.github.cloudiator.iaas.common.persistance.domain.ImageDomainRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepository;
import io.github.cloudiator.iaas.discovery.converters.ImageMessageToImageConverter;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messages.Image.ImageQueryResponse;
import org.cloudiator.messaging.MessageInterface;

/**
 * Created by daniel on 01.06.17.
 */
public class LocationQuerySubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final ImageDomainRepository imageDomainRepository;
  private final TenantModelRepository tenantModelRepository;
  private final ImageMessageToImageConverter imageConverter;

  @Inject
  public LocationQuerySubscriber(MessageInterface messageInterface,
      ImageDomainRepository imageDomainRepository,
      TenantModelRepository tenantModelRepository,
      ImageMessageToImageConverter imageConverter) {
    this.messageInterface = messageInterface;
    this.imageDomainRepository = imageDomainRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.imageConverter = imageConverter;
  }

  @Override
  public void run() {
    messageInterface.subscribe(ImageQueryRequest.class, ImageQueryRequest.parser(),
        (requestId, imageQueryRequest) -> {

          //todo check if user exists?

          List<Image> images = imageDomainRepository.findAll(imageQueryRequest.getUserId());
          final ImageQueryResponse imageQueryResponse = ImageQueryResponse.newBuilder()
              .addAllImages(images.stream().map(imageConverter::applyBack).collect(
                  Collectors.toSet())).build();

          messageInterface.reply(requestId, imageQueryResponse);
        });
  }
}
