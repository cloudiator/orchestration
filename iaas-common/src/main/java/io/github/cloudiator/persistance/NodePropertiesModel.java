package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
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
  private Double disk;

  @OneToOne(optional = true)
  private OperatingSystemModel operatingSystem;

  @OneToOne(optional = true)
  private GeoLocationModel geoLocation;

}
