package io.github.cloudiator.domain;

public class EnvironmentImpl implements Environment {

  private final Runtime runtime;

  EnvironmentImpl(Runtime runtime) {
    this.runtime = runtime;
  }

  @Override
  public Runtime getRuntime() {
    return runtime;
  }
}
