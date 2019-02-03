/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.orchestration.installer.tools.installer;

import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.domain.Node;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * todo clean up class, do better logging
 */
public class WindowsInstaller extends AbstractInstaller {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(WindowsInstaller.class);
  private static final String JAVA_EXE = "jre8.exe";
  private static final String SEVEN_ZIP_ARCHIVE = "7za920.zip";
  private static final String SEVEN_ZIP_DIR = "7zip";
  private static final String SEVEN_ZIP_EXE = "7za.exe";
  private static final String VISOR_BAT = "startVisor.bat";
  private static final String KAIROSDB_BAT = "startKairos.bat";
  private static final String LANCE_BAT = "startLance.bat";
  private static final boolean KAIROS_REQUIRED = true;
  //Play.application().configuration()
  //  .getBoolean("colosseum.installer.windows.kairosdb.install.flag");
  private static final String JAVA_DOWNLOAD = "";
  //Play.application().configuration().getString("colosseum.installer.windows.java.download");
  private static final String SEVEN_ZIP_DOWNLOAD = "";
  private final String homeDir = "";
  //Play.application().configuration().getString("colosseum.installer.windows.7zip.download");

    /*
    private final String user;
    private final String password;
    private final Tenant tenant;
    */


  public WindowsInstaller(RemoteConnection remoteConnection, Node node, String userId) {
    super(remoteConnection, node, userId);

        /*
        this.user = virtualMachine.loginCredential().get().username().get();
        checkArgument(virtualMachine.loginCredential().get().password().isPresent(),
            "Expected login password for WindowsInstaller");
        this.password = virtualMachine.loginCredential().get().password().get();
        this.homeDir =
            virtualMachine.image().get().operatingSystem().operatingSystemFamily().operatingSystemType()
                .homeDirFunction().apply(this.user);
        this.tenant = tenant;
        */

  }

  @Override
  public void bootstrap() throws RemoteException {
    //TODO
  }

  public void initSources() {

    //java
    this.sourcesList.add("powershell -command (new-object System.Net.WebClient).DownloadFile('"
        + WindowsInstaller.JAVA_DOWNLOAD + "','" + this.homeDir + "\\"
        + WindowsInstaller.JAVA_EXE + "')");
    //7zip
    this.sourcesList.add("powershell -command (new-object System.Net.WebClient).DownloadFile('"
        + WindowsInstaller.SEVEN_ZIP_DOWNLOAD + "','" + this.homeDir + "\\"
        + WindowsInstaller.SEVEN_ZIP_ARCHIVE + "')");
    //download visor
    this.sourcesList.add("powershell -command (new-object System.Net.WebClient).DownloadFile('"
        + VISOR_DOWNLOAD + "','" + this.homeDir + "\\"
        + VISOR_JAR + "')");
    if (KAIROS_REQUIRED) {
      //download kairosDB
      this.sourcesList.add(
          "powershell -command (new-object System.Net.WebClient).DownloadFile('"
              + KAIROSDB_DOWNLOAD + "','" + this.homeDir + "\\"
              + KAIROSDB_ARCHIVE + "')");
    }
    //lance
    this.sourcesList.add("powershell -command (new-object System.Net.WebClient).DownloadFile('"
        + LANCE_DOWNLOAD + "','" + this.homeDir + "\\"
        + LANCE_JAR + "')");


  }


  public void installJava() throws RemoteException {

    LOGGER.debug("Installing Java...");
    this.remoteConnection.executeCommand(
        "powershell -command " + this.homeDir + "\\jre8.exe /s INSTALLDIR=" + this.homeDir
            + "\\" + JAVA_DIR);

    //Set JAVA envirnonment vars, use SETX for setting the vars for all future session use /m for machine scope
    remoteConnection.executeCommand(
        "SETX PATH %PATH%;" + this.homeDir + "\\" + JAVA_DIR + "\\bin /m");
    remoteConnection.executeCommand(
        "SETX JAVA_HOME " + this.homeDir + "\\" + JAVA_DIR + " /m");

    LOGGER.debug("Java successfully installed!");


  }

  private void install7Zip() throws RemoteException {
    LOGGER.debug("Unzipping 7zip...");
    this.remoteConnection.executeCommand(
        "powershell -command & { Add-Type -A 'System.IO.Compression.FileSystem'; [IO.Compression.ZipFile]::ExtractToDirectory('"
            + this.homeDir + "\\" + WindowsInstaller.SEVEN_ZIP_ARCHIVE + "', '" + this.homeDir
            + "\\" + WindowsInstaller.SEVEN_ZIP_DIR + "'); }");
    LOGGER.debug("7zip successfully unzipped!");
  }

  @Override
  public void installVisor() throws RemoteException {

    LOGGER.debug("Setting up and starting Visor");

    //create properties file
    this.remoteConnection.writeFile(this.homeDir + "\\" + VISOR_PROPERTIES,
        this.buildDefaultVisorConfig(), false);

    //id of the visor schtasks
    String visorJobId = "visor";

    //create a .bat file to start visor, because it is not possible to pass schtasks paramters using overthere
    String startCommand =
        "java -jar " + this.homeDir + "\\" + VISOR_JAR + " -conf "
            + this.homeDir + "\\" + VISOR_PROPERTIES;
    this.remoteConnection
        .writeFile(this.homeDir + "\\" + WindowsInstaller.VISOR_BAT, startCommand, false);

    //set firewall rules
    this.remoteConnection.executeCommand(
        "powershell -command netsh advfirewall firewall add rule name = 'Visor Rest Port' dir = in action = allow protocol=TCP localport="
            + "");
    //+ Play.application().configuration()
    //.getString("colosseum.installer.abstract.visor.config.restPort"));
    this.remoteConnection.executeCommand(
        "powershell -command netsh advfirewall firewall add rule name = 'Visor Telnet Port' dir = in action = allow protocol=TCP localport="

            + "2");
    //+ Play.application().configuration()
    //.getString("colosseum.installer.abstract.visor.config.telnetPort"));

        /*
        //create schtaks
        this.remoteConnection.executeCommand(
            "schtasks.exe " + "/create " + "/st 00:00  " + "/sc ONCE " + "/ru " + this.user + " "
                + "/rp " + this.password + " " + "/tn " + visorJobId + " /tr \"" + this.homeDir
                + "\\" + WindowsInstaller.VISOR_BAT + "\"");
        this.waitForSchtaskCreation();
        //run schtask
        this.remoteConnection.executeCommand("schtasks.exe /run /tn " + visorJobId);
        */
    LOGGER.debug("Visor started successfully!");

  }

  @Override
  public void installKairosDb() throws RemoteException {

    if (KAIROS_REQUIRED) {
      LOGGER.debug("Extract, setup and start KairosDB...");
      //extract kairosdb
      this.remoteConnection.executeCommand(
          "powershell -command " + this.homeDir + "\\" + WindowsInstaller.SEVEN_ZIP_DIR + "\\"
              + WindowsInstaller.SEVEN_ZIP_EXE + " e " + this.homeDir + "\\"
              + KAIROSDB_ARCHIVE + " -o" + this.homeDir);
      String kairosDbTar = KAIROSDB_ARCHIVE.replace(".gz", "");
      this.remoteConnection.executeCommand(
          "powershell -command " + this.homeDir + "\\" + WindowsInstaller.SEVEN_ZIP_DIR + "\\"
              + WindowsInstaller.SEVEN_ZIP_EXE + " x " + this.homeDir + "\\" + kairosDbTar
              + " -o" + this.homeDir);

      //set firewall rule
      this.remoteConnection.executeCommand(
          "powershell -command netsh advfirewall firewall add rule name = 'Kairos Port' dir = in action = allow protocol=TCP localport="
              //+ Play.application().configuration()
              //.getString("colosseum.installer.abstract.visor.config.kairosPort"));
              + "");

      //create a .bat file to start kairosDB, because it is not possible to pass schtasks paramters using overthere
      String startCommand =
          this.homeDir + "\\" + KAIRROSDB_DIR + "\\bin\\kairosdb.bat run ";
      this.remoteConnection
          .writeFile(this.homeDir + "\\" + WindowsInstaller.KAIROSDB_BAT, startCommand,
              false);

            /*
            //start kairosdb in backround
            String kairosJobId = "kairosDB";
            this.remoteConnection.executeCommand(
                "schtasks.exe /create " + "/st 00:00  " + "/sc ONCE " + "/ru " + this.user + " "
                    + "/rp " + this.password + " " + "/tn " + kairosJobId + " " + "/tr \""
                    + this.homeDir + "\\" + WindowsInstaller.KAIROSDB_BAT + "\"");
            this.waitForSchtaskCreation();
            this.remoteConnection.executeCommand("schtasks.exe /run /tn " + kairosJobId);
            LOGGER.debug("KairosDB successfully started!");
            */
    }

  }

  @Override
  public void installLance() throws RemoteException {
    LOGGER.error("Setting up Lance...");

    LOGGER.error("Opening Firewall ports for Lance...");
    this.remoteConnection.executeCommand(
        "powershell -command netsh advfirewall firewall add rule name = 'Lance RMI' dir = in action = allow protocol=TCP localport="
            + "");
    //+ Play.application().configuration()
    //.getString("colosseum.installer.abstract.lance.rmiPort"));
    this.remoteConnection.executeCommand(
        "powershell -command netsh advfirewall firewall add rule name = 'Lance Server' dir = in action = allow protocol=TCP localport="
            + "");
    //+ Play.application().configuration()
    //.getString("colosseum.installer.abstract.lance.serverPort"));

    //create a .bat file to start Lance, because it is not possible to pass schtasks paramters using overthere
    String startCommand =
        " java " + " -Dhost.ip.public=" + node.ipAddresses().stream()
            .filter(p -> p.type() == IpAddress.IpAddressType.PUBLIC).findAny().get()
            + " -Dhost.ip.private=" + node.ipAddresses().stream()
            .filter(p -> p.type() == IpAddress.IpAddressType.PRIVATE).findAny().get()
            + " -Djava.rmi.server.hostname=" + node.ipAddresses().stream()
            .filter(p -> p.type() == IpAddress.IpAddressType.PUBLIC).findAny().get()
            + " -Dhost.vm.id=" + node.id()
            + " -Dhost.vm.cloud.tenant.id=" + this.userId + " -Dhost.vm.cloud.id="
            + node.id() + " -jar " + this.homeDir + "\\"
            + LANCE_JAR;
    this.remoteConnection
        .writeFile(this.homeDir + "\\" + WindowsInstaller.LANCE_BAT, startCommand, false);

        /*
        //start lance in backround
        String lanceJobId = "lance";
        this.remoteConnection.executeCommand(
            "schtasks.exe " + "/create " + "/st 00:00  " + "/sc ONCE " + "/ru " + this.user + " "
                + "/rp " + this.password + " " + "/tn " + lanceJobId + " " + "/tr \"" + this.homeDir
                + "\\" + WindowsInstaller.LANCE_BAT + "\"");
        this.waitForSchtaskCreation();
        this.remoteConnection.executeCommand("schtasks.exe /run /tn " + lanceJobId);
        LOGGER.debug("Lance successfully started!");
        */

  }


  public void installAll() throws RemoteException {

    LOGGER.debug("Starting installation of all tools on WINDOWS...");

    this.initSources();
    this.downloadSources();

    this.installJava();

    this.installLance();

    this.install7Zip();
    this.installKairosDb();

    this.installVisor();
  }

  @Override
  public void installDocker() throws RemoteException {
    throw new UnsupportedOperationException(
        "Docker installation is currently not supported for Windows!");
  }

  @Override
  public void installSparkWorker() throws RemoteException {
    throw new UnsupportedOperationException(
        "Spark Worker installation is currently not supported for Windows as this tool requires Docker!");
  }

  @Override
  public void installEMS() throws RemoteException {

    // Print node information
    LOGGER.debug(String.format("Node information: id=%s, name=%s, type=%s", node.id(), node.name(), node.type()));
    LOGGER.debug(String.format("Node public addresses: %s", node.publicIpAddresses()));
    LOGGER.debug(String.format("Node 'connectTo' addresses: %s", node.connectTo()));

    // Prepare EMS url to invoke
    String emsUrl = Configuration.conf().getString("installer.ems.url");
    String emsApiKey = Configuration.conf().getString("installer.ems.api-key");
    if (emsApiKey!=null && !emsApiKey.isEmpty()) emsUrl = emsUrl + "?ems-api-key=" + emsApiKey;
    LOGGER.debug(String.format("EMS Server url: %s", emsUrl));

    if (StringUtils.isNotBlank(emsUrl)) {
      // Contact EMS to get EMS Client installation instructions for this node
      LOGGER.debug(String.format("Contacting EMS Server to retrieve EMS Client installation info for node %s: url=%s", node.id(), emsUrl));
      InstallerHelper.InstallationInstructions installationInstructions = InstallerHelper.getInstallationInstructionsFromServer(node, emsUrl);
      LOGGER.debug(String.format("Installation instructions for node %s: %s", node.id(), installationInstructions));

      // Execute installation instructions
      LOGGER.debug(String.format("Executing EMS Client installation instructions on node %s", node.id()));
      InstallerHelper.executeInstructions(node, remoteConnection, installationInstructions);

      LOGGER.debug(String.format("EMS Client installation completed on node %s", node.id()));
    } else {
      LOGGER.warn(String.format("EMS Client installation is switched off"));
    }
  }


}
