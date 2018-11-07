package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.util.stateMachine.Stateful;
import java.util.Optional;

public interface ExtendedCloud extends Cloud, Stateful {

  CloudState state();

  Optional<String> diagnostic();

  String userId();

}
