package io.github.cloudiator.domain;

import java.util.Collection;

public class NodeGroups {

  private NodeGroups() {
    throw new AssertionError("Do not instantiate");
  }

  public static NodeGroup of(Collection<Node> nodes) {
    return new NodeGroupImpl(nodes);
  }

  public static NodeGroup ofSingle(Node node) {
    return new NodeGroupImpl(node);
  }

}
