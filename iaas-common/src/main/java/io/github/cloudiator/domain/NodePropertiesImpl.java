package io.github.cloudiator.domain;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import java.util.Optional;
import javax.annotation.Nullable;

public class NodePropertiesImpl implements NodeProperties {

  private final String providerId;
  private final int numberOfCores;
  private final long memory;
  @Nullable
  private final Double disk;
  @Nullable
  private final OperatingSystem operatingSystem;
  @Nullable
  private final GeoLocation geoLocation;

  NodePropertiesImpl(String providerId, int numberOfCores, long memory, @Nullable Double disk,
      @Nullable OperatingSystem operatingSystem,
      @Nullable GeoLocation geoLocation) {
    this.providerId = providerId;
    this.numberOfCores = numberOfCores;
    this.memory = memory;
    this.disk = disk;
    this.operatingSystem = operatingSystem;
    this.geoLocation = geoLocation;
  }

  @Override
  public String providerId() {
    return providerId;
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
  public Optional<Double> disk() {
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("providerId", providerId)
        .add("numberOfCores", numberOfCores).add("memory", memory).add("disk", disk)
        .add("os", operatingSystem).add("geoLocation", geoLocation).toString();
  }
}
