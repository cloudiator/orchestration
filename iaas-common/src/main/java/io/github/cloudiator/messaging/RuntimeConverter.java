package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.Runtime;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class RuntimeConverter implements TwoWayConverter<MatchmakingEntities.Runtime, Runtime> {

  public static final RuntimeConverter INSTANCE = new RuntimeConverter();

  @Override
  public MatchmakingEntities.Runtime applyBack(Runtime runtime) {
    switch (runtime) {
      case NODEJS:
        return MatchmakingEntities.Runtime.NODEJS;
      case PYTHON:
        return MatchmakingEntities.Runtime.PYTHON;
      case JAVA:
        return MatchmakingEntities.Runtime.JAVA;
      case DOTNET:
        return MatchmakingEntities.Runtime.DOTNET;
      case GO:
        return MatchmakingEntities.Runtime.GO;
      default:
        throw new IllegalStateException("Runtime type not known " + runtime.toString());
    }
  }

  @Override
  public Runtime apply(MatchmakingEntities.Runtime runtime) {
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
