package io.github.cloudiator.iaas.common.messaging.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import io.github.cloudiator.iaas.common.messaging.converters.CloudMessageToCloudConverter;
import io.github.cloudiator.iaas.common.util.CollectorsUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.CloudService;

public class CloudMessageRepository implements MessageRepository<Cloud> {

  private final static String RESPONSE_ERROR = "Could not retrieve cloud object(s) due to error %s";
  private final CloudService cloudService;
  private final CloudMessageToCloudConverter converter = new CloudMessageToCloudConverter();

  public CloudMessageRepository(CloudService cloudService) {
    checkNotNull(cloudService, "cloudService is null");
    this.cloudService = cloudService;
  }

  @Override
  public Cloud getById(String userId, String id) {
    try {
      return cloudService
          .getClouds(CloudQueryRequest.newBuilder().setUserId(userId).setCloudId(id).build())
          .getCloudsList().stream()
          .map(converter)
          .collect(CollectorsUtil.singletonCollector());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }

  @Override
  public List<Cloud> getAll(String userId) {
    try {
      return cloudService.getClouds(CloudQueryRequest.newBuilder().setUserId(userId).build())
          .getCloudsList().stream().map(converter).collect(
              Collectors.toList());
    } catch (ResponseException e) {
      throw new IllegalStateException(String.format(RESPONSE_ERROR, e.getMessage()), e);
    }
  }
}
