package io.github.cloudiator.orchestration.installer.tools.installer;

/**
 * Created by Daniel Seybold on 17.05.2018.
 */

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 19.05.2015.
 */
public class CommandTask implements Callable<Integer> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DownloadTask.class);

  private final RemoteConnection remoteConnection;
  private final String command;

  public CommandTask(RemoteConnection remoteConnection, String command) {
    this.remoteConnection = remoteConnection;
    this.command = command;

  }

  @Override
  public Integer call() throws RemoteException {
    LOGGER.debug("Executing command: " + this.command);

    Integer exitCode = this.remoteConnection.executeCommand(this.command).getExitStatus();

    if(exitCode.intValue() != 0){
      throw new RemoteException("Execution of command: " + command + " failed!");
    }

    return exitCode;
  }
}