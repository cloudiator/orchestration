package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;

public class NodeCandidateBuilder {

  private Cloud cloud;
  private Image image;
  private HardwareFlavor hardwareFlavor;
  private Location location;
  private double price;

  private NodeCandidateBuilder() {}

  public static NodeCandidateBuilder create() {
    return new NodeCandidateBuilder();
  }

  public NodeCandidateBuilder cloud(Cloud cloud) {
    this.cloud = cloud;
    return this;
  }

  public NodeCandidateBuilder image(Image image) {
    this.image = image;
    return this;
  }

  public NodeCandidateBuilder hardware(HardwareFlavor hardwareFlavor) {
    this.hardwareFlavor = hardwareFlavor;
    return this;
  }

  public NodeCandidateBuilder location(Location location) {
    this.location = location;
    return this;
  }

  public NodeCandidateBuilder price(double price) {
    this.price = price;
    return this;
  }

  public NodeCandidate build() {
    return new NodeCandidateImpl(cloud, image, hardwareFlavor, location, price);
  }
}
