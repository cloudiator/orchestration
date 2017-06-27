package io.github.cloudiator.iaas.discovery.messaging;

import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import io.github.cloudiator.iaas.common.persistance.domain.HardwareDomainRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepository;
import io.github.cloudiator.iaas.common.persistance.messaging.converters.HardwareMessageToHardwareConverter;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messages.Hardware.HardwareQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;

/**
 * Created by daniel on 01.06.17.
 */
public class HardwareQuerySubscriber implements Runnable {

  private final MessageInterface messageInterface;
  private final HardwareDomainRepository hardwareDomainRepository;
  private final TenantModelRepository tenantModelRepository;
  private final HardwareMessageToHardwareConverter hardwareConverter;

  @Inject
  public HardwareQuerySubscriber(MessageInterface messageInterface,
      HardwareDomainRepository hardwareDomainRepository,
      TenantModelRepository tenantModelRepository,
      HardwareMessageToHardwareConverter hardwareConverter) {
    this.messageInterface = messageInterface;
    this.hardwareDomainRepository = hardwareDomainRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.hardwareConverter = hardwareConverter;
  }


  @Override
  public void run() {
    Subscription subscription = messageInterface
        .subscribe(HardwareQueryRequest.class, HardwareQueryRequest.parser(),
            (requestId, hardwareQueryRequest) -> {

              //todo check if user exists?

              List<HardwareFlavor> hardwareFlavors = hardwareDomainRepository
                  .findAll(hardwareQueryRequest.getUserId());

              final HardwareQueryResponse hardwareQueryResponse = HardwareQueryResponse.newBuilder()
                  .addAllHardwareFlavors(
                      hardwareFlavors.stream().map(hardwareConverter::applyBack)
                          .collect(Collectors.toList())).build();

              messageInterface.reply(requestId, hardwareQueryResponse);
            });
  }
}
