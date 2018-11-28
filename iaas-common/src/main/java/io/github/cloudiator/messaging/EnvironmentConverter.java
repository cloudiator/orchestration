package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.Environment;
import io.github.cloudiator.domain.EnvironmentBuilder;
import io.github.cloudiator.domain.Runtime;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class EnvironmentConverter implements
    OneWayConverter<MatchmakingEntities.Environment, Environment> {

  public static final EnvironmentConverter INSTANCE = new EnvironmentConverter();

  @Override
  public Environment apply(MatchmakingEntities.Environment environment) {
    return EnvironmentBuilder.newBuilder()
        .runtime(convertRuntime(environment.getRuntime()))
        .build();
  }

  private Runtime convertRuntime(MatchmakingEntities.Runtime runtime) {
    switch (runtime) {
      case NODEJS:
        return Runtime.NODEJS;
      case PYTHON:
        return Runtime.PYTHON;
      case JAVA:
        return Runtime.JAVA;
      case DOTNET:
        return Runtime.DOTNET;
      case GO:
        return Runtime.GO;
      case UNRECOGNIZED:
      default:
        throw new IllegalStateException("Runtime type not known " + runtime.toString());
    }
  }
}
