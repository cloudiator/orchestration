package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;

public class NodeDomainRepository {

  private final NodeModelRepository nodeModelRepository;

  @Inject
  public NodeDomainRepository(
      NodeModelRepository nodeModelRepository) {
    this.nodeModelRepository = nodeModelRepository;
  }

  public Node findByTenantAndId(String userId, String imageId) {
    return null;
  }

}
