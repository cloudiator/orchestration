package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.domain.Identifiable;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Optional;
import java.util.Set;

public interface Node extends Identifiable {

  NodeProperties nodeProperties();

  Optional<LoginCredential> loginCredential();

  NodeType type();

  Set<IpAddress> ipAddresses();

  IpAddress connectTo();
}
