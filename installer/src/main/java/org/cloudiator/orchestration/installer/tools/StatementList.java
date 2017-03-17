package org.cloudiator.orchestration.installer.tools;

import java.util.List;

/**
 * Created by daniel on 08.02.17.
 */
public interface StatementList extends Iterable<Statement> {

    List<Statement> statements();

}
