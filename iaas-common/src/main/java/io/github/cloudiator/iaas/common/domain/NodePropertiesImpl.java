package io.github.cloudiator.iaas.common.domain;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import java.util.Optional;
import javax.annotation.Nullable;

public class NodePropertiesImpl implements NodeProperties {

  private final int numberOfCores;
  private final long memory;
  @Nullable
  private final Float disk;
  @Nullable
  private final OperatingSystem operatingSystem;
  @Nullable
  private final GeoLocation geoLocation;

  NodePropertiesImpl(int numberOfCores, long memory, @Nullable Float disk,
      @Nullable OperatingSystem operatingSystem,
      @Nullable GeoLocation geoLocation) {
    this.numberOfCores = numberOfCores;
    this.memory = memory;
    this.disk = disk;
    this.operatingSystem = operatingSystem;
    this.geoLocation = geoLocation;
  }

  @Override
  public int numberOfCores() {
    return numberOfCores;
  }

  @Override
  public long memory() {
    return memory;
  }

  @Override
  public Optional<Float> disk() {
    return Optional.ofNullable(disk);
  }

  @Override
  public Optional<OperatingSystem> operatingSystem() {

    return Optional.ofNullable(operatingSystem);
  }

  @Override
  public Optional<GeoLocation> geoLocation() {
    return Optional.ofNullable(geoLocation);
  }
}
