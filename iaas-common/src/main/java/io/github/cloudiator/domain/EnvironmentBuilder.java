package io.github.cloudiator.domain;

public class EnvironmentBuilder {

  private Runtime runtime;

  private EnvironmentBuilder() {
  }

  public static EnvironmentBuilder newBuilder() {
    return new EnvironmentBuilder();
  }

  public EnvironmentBuilder runtime(Runtime runtime) {
    this.runtime = runtime;
    return this;
  }

  public Environment build() {
    return new EnvironmentImpl(runtime);
  }
}
