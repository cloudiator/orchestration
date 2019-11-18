package io.github.cloudiator.orchestration.installer.tools.installer;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnectionResponse;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdempotencyValidator {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(IdempotencyValidator.class);

  // do not instantiate
  private IdempotencyValidator() {
  }

  public static boolean checkIsInstalledViaProcess(RemoteConnection remoteConnection,
      String regExString) throws RemoteException {
    RemoteConnectionResponse checkResult = remoteConnection
        .executeCommand("ps -ef | grep -c " + regExString);

    return checkResult.getExitStatus() == 0 ? true : false;
  }

  public static boolean checkIsInstalledViaFolder(RemoteConnection remoteConnection,
      String fullPath) throws RemoteException {
    RemoteConnectionResponse checkResult = remoteConnection
        .executeCommand("cd " + fullPath);

    return checkResult.getExitStatus() == 0 ? true : false;
  }

  public static int checkIsInstalledViaCommand(RemoteConnection remoteConnection,
      String command) throws RemoteException {
    RemoteConnectionResponse checkResult = remoteConnection
        .executeCommand("" + command);
    return checkResult.getExitStatus();
  }

  public static boolean checkIsInstalledDocker(RemoteConnection remoteConnection)
      throws RemoteException {
    //check for installed Docker
    final boolean isInstalled = checkIsInstalledViaProcess(remoteConnection, "\"[d]ockerd\"");

    LOGGER.debug(
        String.format("Exit code of dockerd process-search is %s", isInstalled ? "0" : "!=0"));

    return isInstalled;
  }

  public static boolean checkIsInstalledLance(RemoteConnection remoteConnection)
      throws RemoteException {
    //check for installed Lance
    final boolean isInstalled = checkIsInstalledViaProcess(remoteConnection, "\"[l]ance.jar\"");

    LOGGER
        .debug(String.format("Exit code of lance process-search is %s", isInstalled ? "0" : "!=0"));

    return isInstalled;
  }

  public static boolean checkIsInstalledVisor(RemoteConnection remoteConnection)
      throws RemoteException {
    //check for installed Visor
    final boolean isInstalled = checkIsInstalledViaProcess(remoteConnection, "\"[v]isor.jar\"");

    LOGGER
        .debug(String.format("Exit code of visor process-search is %s", isInstalled ? "0" : "!=0"));

    return isInstalled;
  }

  public static boolean checkIsInstalledEMS(RemoteConnection remoteConnection)
      throws RemoteException {
    //check for installed EMS-Client = Baguette-Client
    final boolean isInstalled = checkIsInstalledViaFolder(remoteConnection,
        "/opt/baguette-client/");

    LOGGER.debug(String.format("Exit code of ems-folder-search is %s", isInstalled ? "0" : "!=0"));

    return isInstalled;
  }

  public static boolean checkIsInstalledJava(RemoteConnection remoteConnection)
      throws RemoteException {
    //check for installed Java
    final int isInstalled = checkIsInstalledViaCommand(remoteConnection, "java -version");

    LOGGER.debug(String.format("Exit code of java -version is %s", isInstalled));

    return (isInstalled == 0);
  }
}
