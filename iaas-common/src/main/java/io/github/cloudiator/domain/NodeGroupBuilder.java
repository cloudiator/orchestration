package io.github.cloudiator.domain;

import java.util.List;

/**
 * Created by Daniel Seybold on 28.11.2018.
 */
public class NodeGroupBuilder {

  private List<Node> nodes;
  private String id;

  private NodeGroupBuilder(){

  }

  public static NodeGroupBuilder newBuilder() {
    return new NodeGroupBuilder();
  }

  public NodeGroupBuilder nodes(
      List<Node> nodes) {
    this.nodes = nodes;
    return this;
  }

  public NodeGroupBuilder id(String id) {
    this.id = id;
    return this;
  }

  public NodeGroup build() {
    return new NodeGroupImpl(id, nodes);
  }

}
