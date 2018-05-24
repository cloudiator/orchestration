package io.github.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;

public class NodeGroupImpl implements NodeGroup {

  private final List<Node> nodes;

  NodeGroupImpl(Collection<Node> nodes) {
    checkNotNull(nodes, "nodes is null");
    this.nodes = ImmutableList.copyOf(nodes);
  }

  NodeGroupImpl(Node node) {
    checkNotNull(node, "node is null");
    this.nodes = ImmutableList.of(node);
  }

  @Override
  public List<Node> getNodes() {
    return nodes;
  }
}
