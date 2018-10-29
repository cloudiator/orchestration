package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;

import java.util.Set;

public interface NodeCandidate {

  NodeCandidateType type();

  Cloud cloud();

  Image image();

  HardwareFlavor hardware();

  Location location();

  double price();

  double pricePerInvocation();

  double memoryPrice();

  Environment environment();

}
