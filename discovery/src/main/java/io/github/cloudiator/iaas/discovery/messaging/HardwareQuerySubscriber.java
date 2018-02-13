package io.github.cloudiator.iaas.discovery.messaging;

import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import io.github.cloudiator.messaging.HardwareMessageToHardwareConverter;
import io.github.cloudiator.persistance.HardwareDomainRepository;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messages.Hardware.HardwareQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class HardwareQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(HardwareQuerySubscriber.class);
  private final MessageInterface messageInterface;
  private final HardwareDomainRepository hardwareDomainRepository;
  private final HardwareMessageToHardwareConverter hardwareConverter;

  @Inject
  public HardwareQuerySubscriber(MessageInterface messageInterface,
      HardwareDomainRepository hardwareDomainRepository,
      HardwareMessageToHardwareConverter hardwareConverter) {
    this.messageInterface = messageInterface;
    this.hardwareDomainRepository = hardwareDomainRepository;
    this.hardwareConverter = hardwareConverter;
  }


  @Override
  public void run() {
    Subscription subscription = messageInterface
        .subscribe(HardwareQueryRequest.class, HardwareQueryRequest.parser(),
            (requestId, hardwareQueryRequest) -> {

              try {
                decideAndReply(requestId, hardwareQueryRequest);
              } catch (Exception e) {
                LOGGER.error(String
                    .format("Caught exception %s during execution of %s", e.getMessage(), this), e);
              }
            });
  }

  private void decideAndReply(String requestId, HardwareQueryRequest request) {
    if (request.getUserId().isEmpty()) {
      replyErrorNoUserId(requestId);
      return;
    }
    if (!request.getHardwareId().isEmpty()) {
      replyForUserIdAndHardwareId(requestId, request.getUserId(), request.getHardwareId());
      return;
    }
    if (!request.getCloudId().isEmpty()) {
      replyForUserIdAndCloudId(requestId, request.getUserId(), request.getCloudId());
      return;
    }
    replyForUserId(requestId, request.getUserId());
  }

  private void replyErrorNoUserId(String requestId) {
    messageInterface.reply(HardwareQueryResponse.class, requestId,
        Error.newBuilder().setCode(500).setMessage("Request does not contain userId.")
            .build());
  }


  private void replyForUserIdAndHardwareId(String requestId, String userId, String hardwareId) {
    final HardwareFlavor hardwareFlavor = hardwareDomainRepository
        .findByTenantAndId(userId, hardwareId);
    if (hardwareFlavor == null) {
      messageInterface.reply(HardwareQueryResponse.class, requestId,
          Error.newBuilder().setCode(404)
              .setMessage(String.format("Hardware with id %s was not found.", hardwareId))
              .build());
    } else {
      HardwareQueryResponse hardwareQueryResponse = HardwareQueryResponse.newBuilder()
          .addHardwareFlavors(hardwareConverter.applyBack(hardwareFlavor)).build();
      messageInterface.reply(requestId, hardwareQueryResponse);
    }

  }

  private void replyForUserIdAndCloudId(String requestId, String userId, String cloudId) {
    HardwareQueryResponse hardwareQueryResponse = HardwareQueryResponse.newBuilder()
        .addAllHardwareFlavors(
            hardwareDomainRepository.findByTenantAndCloud(userId, cloudId).stream().map(
                hardwareConverter::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, hardwareQueryResponse);
  }

  private void replyForUserId(String requestId, String userId) {
    HardwareQueryResponse hardwareQueryResponse = HardwareQueryResponse.newBuilder()
        .addAllHardwareFlavors(hardwareDomainRepository.findAll(userId).stream().map(
            hardwareConverter::applyBack).collect(Collectors.toList())).build();
    messageInterface.reply(requestId, hardwareQueryResponse);
  }

}
