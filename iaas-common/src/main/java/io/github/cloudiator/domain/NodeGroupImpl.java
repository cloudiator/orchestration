package io.github.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;

public class NodeGroupImpl implements NodeGroup {

  private final List<Node> nodes;
  private final String id;

  NodeGroupImpl(String id, Collection<Node> nodes) {
    checkNotNull(id, "id is null");
    checkNotNull(nodes, "nodes is null");
    this.nodes = ImmutableList.copyOf(nodes);
    this.id = id;
  }

  NodeGroupImpl(String id, Node node) {
    checkNotNull(id, "id is null");
    checkNotNull(node, "node is null");
    this.nodes = ImmutableList.of(node);
    this.id = id;
  }

  @Override
  public List<Node> getNodes() {
    return nodes;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("nodes", Joiner.on(",").join(nodes))
        .toString();
  }
}
