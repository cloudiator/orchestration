package org.cloudiator.iaas.node;

import com.google.common.base.MoreObjects;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class CompositeNodeDeletionStrategy implements NodeDeletionStrategy {

  private final Set<NodeDeletionStrategy> nodeDeletionStrategies;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CompositeNodeDeletionStrategy.class);

  @Inject
  public CompositeNodeDeletionStrategy(
      Set<NodeDeletionStrategy> nodeDeletionStrategies) {
    this.nodeDeletionStrategies = nodeDeletionStrategies;
  }

  @Override
  public boolean supportsNode(Node node) {
    for (NodeDeletionStrategy nodeDeletionStrategy : nodeDeletionStrategies) {
      if (nodeDeletionStrategy.supportsNode(node)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean deleteNode(Node node, String userId) {

    checkNotNull(node, "node is null");
    checkNotNull(userId, "userId is null");

    for (NodeDeletionStrategy nodeDeletionStrategy : nodeDeletionStrategies) {
      if (nodeDeletionStrategy.supportsNode(node)) {
        LOGGER.debug(String
            .format("%s is using strategy %s to delete node %s.", this, nodeDeletionStrategy,
                node));
        return nodeDeletionStrategy.deleteNode(node, userId);
      }
    }

    throw new IllegalStateException(
        String.format("%s did not find a suitable strategy to delete node %s.", this, node));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("nodeDeletionStrategies", nodeDeletionStrategies)
        .toString();
  }
}
