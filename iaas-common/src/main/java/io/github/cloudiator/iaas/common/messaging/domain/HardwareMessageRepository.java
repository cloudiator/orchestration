package io.github.cloudiator.iaas.common.messaging.domain;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import io.github.cloudiator.iaas.common.messaging.converters.HardwareMessageToHardwareConverter;
import io.github.cloudiator.iaas.common.util.CollectorsUtil;
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
      return hardwareService.getHardware(
          HardwareQueryRequest.newBuilder().setUserId(userId).setHardwareId(id).build())
          .getHardwareFlavorsList().stream().map(converter).collect(
              CollectorsUtil.singletonCollector());
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
