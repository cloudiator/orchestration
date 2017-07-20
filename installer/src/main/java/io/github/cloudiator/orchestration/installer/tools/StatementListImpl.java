package io.github.cloudiator.orchestration.installer.tools;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by daniel on 08.02.17.
 */
public class StatementListImpl implements StatementList {

    private final List<Statement> statements;

    public StatementListImpl(List<Statement> statements) {
        this.statements = ImmutableList.copyOf(statements);
    }

    @Override public List<Statement> statements() {
        return statements;
    }

    @Override public Iterator<Statement> iterator() {
        return statements.iterator();
    }
}
