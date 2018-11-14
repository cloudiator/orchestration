package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import java.util.Optional;

public interface NodeProperties {

  String providerId();

  int numberOfCores();

  long memory();

  Optional<Double> disk();

  Optional<OperatingSystem> operatingSystem();

  Optional<GeoLocation> geoLocation();

}
