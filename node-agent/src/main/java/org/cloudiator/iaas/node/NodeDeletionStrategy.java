package org.cloudiator.iaas.node;

import io.github.cloudiator.domain.Node;

public interface NodeDeletionStrategy {

  boolean supportsNode(Node node);

  boolean deleteNode(Node node, String userId);

}
