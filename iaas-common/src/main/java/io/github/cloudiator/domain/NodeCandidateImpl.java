package io.github.cloudiator.domain;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import java.util.Objects;

public class NodeCandidateImpl implements NodeCandidate {

  private final Cloud cloud;
  private final Image image;
  private final HardwareFlavor hardwareFlavor;
  private final Location location;
  private final double price;


  public NodeCandidateImpl(Cloud cloud, Image image,
      HardwareFlavor hardwareFlavor, Location location, double price) {
    this.cloud = cloud;
    this.image = image;
    this.hardwareFlavor = hardwareFlavor;
    this.location = location;
    this.price = price;
  }

  @Override
  public Cloud cloud() {
    return cloud;
  }

  @Override
  public Image image() {
    return image;
  }

  @Override
  public HardwareFlavor hardware() {
    return hardwareFlavor;
  }

  @Override
  public Location location() {
    return location;
  }

  @Override
  public double price() {
    return price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeCandidateImpl that = (NodeCandidateImpl) o;
    return Double.compare(that.price, price) == 0 &&
        Objects.equals(cloud, that.cloud) &&
        Objects.equals(image, that.image) &&
        Objects.equals(hardwareFlavor, that.hardwareFlavor) &&
        Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cloud, image, hardwareFlavor, location, price);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("cloud", cloud).add("image", image)
        .add("hardware", hardwareFlavor).add("location", location).add("price", price).toString();
  }
}
