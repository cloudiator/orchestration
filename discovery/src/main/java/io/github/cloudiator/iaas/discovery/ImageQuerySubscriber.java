package io.github.cloudiator.iaas.discovery;

import io.github.cloudiator.iaas.common.persistance.entities.ImageModel;
import io.github.cloudiator.iaas.common.persistance.entities.Tenant;
import io.github.cloudiator.iaas.common.persistance.repositories.ResourceRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepository;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;

/**
 * Created by daniel on 01.06.17.
 */
public class ImageQuerySubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final ResourceRepository<ImageModel> imageResourceRepository;
  private final TenantModelRepository tenantModelRepository;

  public ImageQuerySubscriber(MessageInterface messageInterface,
      ResourceRepository<ImageModel> imageResourceRepository,
      TenantModelRepository tenantModelRepository) {
    this.messageInterface = messageInterface;
    this.imageResourceRepository = imageResourceRepository;
    this.tenantModelRepository = tenantModelRepository;
  }

  @Override
  public void run() {
    messageInterface.subscribe(ImageQueryRequest.class, ImageQueryRequest.parser(),
        new MessageCallback<ImageQueryRequest>() {
          @Override
          public void accept(String imageId, ImageQueryRequest imageQueryRequest) {

            Tenant tenant = tenantModelRepository.findByUserId(imageQueryRequest.getUserId());

          }
        });
  }
}
