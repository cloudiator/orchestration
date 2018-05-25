package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;

public class NodeDomainRepository {

  private final NodeModelRepository nodeModelRepository;

  @Inject
  public NodeDomainRepository(
      NodeModelRepository nodeModelRepository) {
    this.nodeModelRepository = nodeModelRepository;
  }

  public Node findByTenantAndId(String userId, String nodeId) {
    return null;
  }

  public void save(Node domain, String userId) {
    checkNotNull(domain, "domain is null");
    checkNotNull(userId, "userId is null");

    final NodeModel nodeModel = nodeModelRepository.getByTenantAndDomainId(userId, domain.id());

    if (nodeModel == null) {
      final NodeModel created = createModel(domain, userId);
      nodeModelRepository.save(created);
    } else {
      final NodeModel updated = updateModel(domain, nodeModel, userId);
      nodeModelRepository.save(updated);
    }
  }

  private NodeModel updateModel(Node domain, NodeModel nodeModel, String userId) {
    return null;
  }

  private NodeModel createModel(Node domain, String userId) {
    return null;
  }

}
