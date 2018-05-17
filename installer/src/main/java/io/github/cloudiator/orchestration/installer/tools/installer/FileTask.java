package io.github.cloudiator.orchestration.installer.tools.installer;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 17.05.2018.
 */
public class FileTask implements Callable<Integer> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DownloadTask.class);

  private final RemoteConnection remoteConnection;
  private final String path;
  private final String content;
  private boolean executable;

  public FileTask(RemoteConnection remoteConnection, String path, String content, boolean executeable) {
    this.remoteConnection = remoteConnection;
    this.path = path;
    this.content = content;
    this.executable = executeable;

  }

  @Override
  public Integer call() throws RemoteException {
    LOGGER.debug("Creating file at " + this.path);

    Integer exitCode = this.remoteConnection.writeFile(this.path, this.content, this.executable);

    if(exitCode.intValue() != 0){
      throw new RemoteException("Creation of file at: " + this.path + " failed!");
    }

    return exitCode;
  }
}
