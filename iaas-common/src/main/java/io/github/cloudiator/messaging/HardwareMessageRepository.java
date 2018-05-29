package io.github.cloudiator.messaging;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.HardwareService;

public class HardwareMessageRepository implements MessageRepository<HardwareFlavor> {

  private final static String RESPONSE_ERROR = "Could not retrieve hardware flavor object(s) due to error %s";
  private final HardwareService hardwareService;
  private final HardwareMessageToHardwareConverter converter = new HardwareMessageToHardwareConverter();

  @Inject
  public HardwareMessageRepository(
      HardwareService hardwareService) {
    this.hardwareService = hardwareService;
  }

  @Override
  public HardwareFlavor getById(String userId, String id) {
    try {
      final List<HardwareFlavor> collect = hardwareService.getHardware(
          HardwareQueryRequest.newBuilder().setUserId(userId).setHardwareId(id).build())
          .getHardwareFlavorsList().stream().map(converter).collect(
              Collectors.toList());

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
  public List<HardwareFlavor> getAll(String userId) {
    try {
      return hardwareService
          .getHardware(HardwareQueryRequest.newBuilder().setUserId(userId).build())
          .getHardwareFlavorsList().stream().map(converter).collect(
              Collectors.toList());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }
}
