package io.github.cloudiator.orchestration.installer.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by daniel on 08.02.17.
 */
public class Statements {

  public static Statement of(String command) {
    return new StatementImpl(command);
  }

  public static StatementList of(Statement... statements) {
    return new StatementListImpl(Arrays.asList(statements));
  }

  public static StatementList of(String... commands) {
    return new StatementListImpl(
        Arrays.stream(commands).map(Statements::of).collect(Collectors.toList()));
  }

  public static StatementList empty() {
    return new StatementListImpl(Collections.emptyList());
  }

}
