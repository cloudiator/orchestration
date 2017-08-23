package io.github.cloudiator.iaas.common.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;


public class NodePropertiesBuilder {

  private int numberOfCores;
  private long memory;
  private Float disk;
  private OperatingSystem operatingSystem;
  private GeoLocation geoLocation;

  private NodePropertiesBuilder() {
  }

  public static NodePropertiesBuilder of(HardwareFlavor hardwareFlavor, Image image,
      Location location) {

    checkNotNull(hardwareFlavor, "hardwareFlavor is null");
    checkNotNull(image, "image is null");
    checkNotNull(location, "location is null");

    return newBuilder().numberOfCores(hardwareFlavor.numberOfCores()).memory(hardwareFlavor.mbRam())
        .disk(hardwareFlavor.gbDisk().orElse(null)).os(image.operatingSystem())
        .geoLocation(location.geoLocation().orElse(null));
  }

  public static NodePropertiesBuilder newBuilder() {
    return new NodePropertiesBuilder();
  }

  public NodePropertiesBuilder numberOfCores(int numberOfCores) {
    this.numberOfCores = numberOfCores;
    return this;
  }

  public NodePropertiesBuilder memory(long memory) {
    this.memory = memory;
    return this;
  }

  public NodePropertiesBuilder disk(Float disk) {
    this.disk = disk;
    return this;
  }

  public NodePropertiesBuilder os(OperatingSystem operatingSystem) {
    this.operatingSystem = operatingSystem;
    return this;
  }

  public NodePropertiesBuilder geoLocation(GeoLocation geoLocation) {
    this.geoLocation = geoLocation;
    return this;
  }

  public NodeProperties build() {
    return new NodePropertiesImpl(numberOfCores, memory, disk, operatingSystem, geoLocation);
  }


}
