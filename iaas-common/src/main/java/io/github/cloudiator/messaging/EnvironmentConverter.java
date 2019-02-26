package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.Environment;
import io.github.cloudiator.domain.EnvironmentBuilder;
import io.github.cloudiator.domain.Runtime;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class EnvironmentConverter implements
    OneWayConverter<MatchmakingEntities.Environment, Environment> {

  public static final EnvironmentConverter INSTANCE = new EnvironmentConverter();
  private final RuntimeConverter runtimeConverter = RuntimeConverter.INSTANCE;

  @Override
  public Environment apply(MatchmakingEntities.Environment environment) {
    return EnvironmentBuilder.newBuilder()
        .runtime(runtimeConverter.apply(environment.getRuntime()))
        .build();
  }
}
