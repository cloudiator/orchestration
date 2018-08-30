package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;

public interface NodeCandidate {

  Cloud cloud();

  Image image();

  HardwareFlavor hardware();

  Location location();

  double price();
}
