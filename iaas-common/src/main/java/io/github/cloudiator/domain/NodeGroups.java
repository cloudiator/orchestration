package io.github.cloudiator.domain;

import java.util.Collection;
import java.util.UUID;

public class NodeGroups {

  private NodeGroups() {
    throw new AssertionError("Do not instantiate");
  }

  private static String generateId() {
    return UUID.randomUUID().toString();
  }

  public static NodeGroup of(Collection<Node> nodes) {
    return new NodeGroupImpl(generateId(), nodes);
  }

  public static NodeGroup of(String id, Collection<Node> nodes) {
    return new NodeGroupImpl(id, nodes);
  }

  public static NodeGroup ofSingle(Node node) {
    return new NodeGroupImpl(generateId(), node);
  }

  public static NodeGroup ofSingle(String id, Node node) {
    return new NodeGroupImpl(id, node);
  }

}
