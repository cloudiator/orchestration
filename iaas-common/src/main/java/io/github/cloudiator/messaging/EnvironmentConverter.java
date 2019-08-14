package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.Environment;
import io.github.cloudiator.domain.EnvironmentBuilder;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class EnvironmentConverter implements
    TwoWayConverter<MatchmakingEntities.Environment, Environment> {

  public static final EnvironmentConverter INSTANCE = new EnvironmentConverter();
  private static final RuntimeConverter RUNTIME_CONVERTER = RuntimeConverter.INSTANCE;

  @Override
  public Environment apply(MatchmakingEntities.Environment environment) {
    return EnvironmentBuilder.newBuilder()
        .runtime(RUNTIME_CONVERTER.apply(environment.getRuntime()))
        .build();
  }

  @Override
  public MatchmakingEntities.Environment applyBack(Environment environment) {
    return MatchmakingEntities.Environment.newBuilder()
        .setRuntime(RUNTIME_CONVERTER.applyBack(environment.getRuntime()))
        .build();
  }
}
