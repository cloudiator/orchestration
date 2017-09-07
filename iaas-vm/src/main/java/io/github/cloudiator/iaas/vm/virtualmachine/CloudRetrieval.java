package io.github.cloudiator.iaas.vm.virtualmachine;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.cloudiator.iaas.common.messaging.CloudMessageToCloudConverter;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.CloudService;

public class CloudRetrieval {

  private final CloudService cloudService;

  public CloudRetrieval(CloudService cloudService) {
    checkNotNull(cloudService, "cloudService is null");
    this.cloudService = cloudService;
  }

  @Nullable
  public Cloud retrieve(String id, String userId) {
    try {
      return cloudService.getClouds(CloudQueryRequest.newBuilder().setUserId(userId).build())
          .getCloudsList().stream().filter(
              cloud -> cloud.getId().equals(id)).findAny()
          .map(cloud -> new CloudMessageToCloudConverter().apply(cloud)).orElse(null);
    } catch (ResponseException e) {
      throw new IllegalStateException("Could not retrieve cloud.", e);
    }
  }
}
