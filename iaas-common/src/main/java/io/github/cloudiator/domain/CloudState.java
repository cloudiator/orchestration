package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.util.stateMachine.State;

public enum CloudState implements State {

  NEW,
  OK,
  ERROR,
  DELETED

}
