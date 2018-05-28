package io.github.cloudiator.persistance;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
class NodePropertiesModel extends Model {

  @Column(nullable = false)
  private int numberOfCores;

  @Column(nullable = false)
  private long memory;

  @Column(nullable = true)
  @Nullable
  private Double disk;

  @OneToOne(optional = true)
  @Nullable
  private OperatingSystemModel operatingSystem;

  @OneToOne(optional = true)
  @Nullable
  private GeoLocationModel geoLocation;

  /**
   * Empty constructor for hibernate.
   */
  protected NodePropertiesModel() {

  }

  NodePropertiesModel(int numberOfCores, long memory, @Nullable Double disk,
      @Nullable OperatingSystemModel operatingSystem, @Nullable GeoLocationModel geoLocation) {

    this.numberOfCores = numberOfCores;
    this.memory = memory;
    this.disk = disk;
    this.operatingSystem = operatingSystem;
    this.geoLocation = geoLocation;

  }

  public int getNumberOfCores() {
    return numberOfCores;
  }

  public long getMemory() {
    return memory;
  }

  @Nullable
  public Double getDisk() {
    return disk;
  }

  @Nullable
  public OperatingSystemModel getOperatingSystem() {
    return operatingSystem;
  }

  @Nullable
  public GeoLocationModel getGeoLocation() {
    return geoLocation;
  }
}
