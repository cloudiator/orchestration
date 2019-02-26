/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.iaas.discovery.messaging;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import io.github.cloudiator.domain.DiscoveredImage;
import io.github.cloudiator.messaging.ImageMessageToImageConverter;
import io.github.cloudiator.persistance.ImageDomainRepository;
import java.util.stream.Collectors;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messages.Image.ImageQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class ImageQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageQuerySubscriber.class);
  private static final ImageMessageToImageConverter IMAGE_CONVERTER = ImageMessageToImageConverter.INSTANCE;
  private final MessageInterface messageInterface;
  private final ImageDomainRepository imageDomainRepository;

  @Inject
  public ImageQuerySubscriber(MessageInterface messageInterface,
      ImageDomainRepository imageDomainRepository) {
    this.messageInterface = messageInterface;
    this.imageDomainRepository = imageDomainRepository;
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
    final DiscoveredImage image = imageDomainRepository
        .findByTenantAndId(userId, imageId);
    if (image == null) {
      messageInterface.reply(requestId, ImageQueryResponse.newBuilder().build());
    } else {
      ImageQueryResponse imageQueryResponse = ImageQueryResponse.newBuilder()
          .addImages(IMAGE_CONVERTER.applyBack(image)).build();
      messageInterface.reply(requestId, imageQueryResponse);
    }

  }

  private void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    ImageQueryResponse imageQueryResponse = ImageQueryResponse.newBuilder()
        .addAllImages(
            imageDomainRepository.findByTenantAndCloud(userId, cloudId).stream().map(
                IMAGE_CONVERTER::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, imageQueryResponse);
  }

  private void replyForUserId(String requestId, String userId) {
    ImageQueryResponse imageQueryResponse = ImageQueryResponse.newBuilder()
        .addAllImages(imageDomainRepository.findAll(userId).stream().map(
            IMAGE_CONVERTER::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, imageQueryResponse);
  }
}
