package org.cloudiator.orchestration.installer.tools;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 08.02.17.
 */
public class StatementImpl implements Statement {

    private final String command;

    StatementImpl(String command) {
        checkNotNull(command, "command is null.");
        checkArgument(!command.isEmpty(), "command is empty.");
        this.command = command;
    }

    @Override public String command() {
        return command;
    }
}
