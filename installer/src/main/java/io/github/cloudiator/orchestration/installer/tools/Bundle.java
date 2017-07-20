package io.github.cloudiator.orchestration.installer.tools;

import java.util.Set;

/**
 * Created by daniel on 08.02.17.
 */
public interface Bundle {

    String name();

    Set<String> dependencies(Environment environment);

    Set<Integer> inboundPorts();

    Set<Download> downloads(Environment environment);

    StatementList statements(Environment environment);

}
