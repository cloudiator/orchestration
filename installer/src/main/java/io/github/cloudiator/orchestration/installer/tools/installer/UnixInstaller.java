/*
 * Copyright (c) 2014-2015 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.orchestration.installer.tools.installer;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.orchestration.installer.tools.DownloadImpl;
import org.cloudiator.messages.NodeEntities.Node;
import org.cloudiator.messages.entities.IaasEntities.IpAddressType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * todo clean up class, do better logging
 */
public class UnixInstaller extends AbstractInstaller {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(UnixInstaller.class);


  private static final String DOCKER_FIX_MTU_INSTALL = "docker_fix_mtu.sh";
  private static final String TOOL_PATH = "/opt/cloudiator/";

  /**
   * shell command to check if the source file of a tool exists
   */
  private static final String CHECK_IF_SOURCE_EXIST_COMMAND = "[ -f \"$\" ] || { exit 99 ;}";

  private final String JAVA_BINARY = UnixInstaller.TOOL_PATH + JAVA_DIR + "/bin/java";
  private static final String JAVA_ARCHIVE = "jre8.tar.gz";
  private static final String JAVA_DOWNLOAD = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=106240";
  //Play.application().configuration().getString("colosseum.installer.linux.java.download");
  private static final String DOCKER_RETRY_DOWNLOAD = "https://raw.githubusercontent.com/cloudiator/lance/master/install/docker_retry_fix_version.sh";
  //Play.application().configuration()
  //.getString("colosseum.installer.linux.lance.docker_retry.download");
  private static final String DOCKER_FIX_MTU_DOWNLOAD = "https://raw.githubusercontent.com/cloudiator/colosseum/master/resources/fix_mtu.sh";
  //Play.application().configuration()
  //  .getString("colosseum.installer.linux.lance.docker.mtu.download");
  private static final String DOCKER_RETRY_INSTALL = "docker_retry.sh";
  private static final boolean KAIROS_REQUIRED = false;
  //Play.application().configuration()
  //  .getBoolean("colosseum.installer.linux.kairosdb.install.flag");
  private static final boolean DOCKER_REQUIRED = false;
  //Play.application().configuration()
  //  .getBoolean("colosseum.installer.linux.lance.docker.install.flag");
  private static final String SNAP_DOWNLOAD = "https://packagecloud.io/install/repositories/intelsdi-x/snap/script.deb.sh";


  public UnixInstaller(RemoteConnection remoteConnection, Node node) {
    super(remoteConnection, node);

  }

  public void initToolDirectory() {

    try {

      LOGGER.debug(String.format(
          "Creating cloudiator tool directory in " + UnixInstaller.TOOL_PATH + " for node %s",
          node.getId()));

      this.remoteConnection.executeCommand("sudo mkdir " + UnixInstaller.TOOL_PATH);


    } catch (RemoteException e) {
      e.printStackTrace();
    }

  }


  @Override
  public void initSources() {

    //java
    this.sourcesList
        .add(
            new DownloadImpl(
                "sudo wget " + UnixInstaller.JAVA_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
                    + UnixInstaller.JAVA_ARCHIVE,
                "UnixInstaller.TOOL_PATH + UnixInstaller.JAVA_ARCHIVE"));
    //lance
    this.sourcesList
        .add(new DownloadImpl(
            "sudo wget " + LANCE_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH + LANCE_JAR,
            "UnixInstaller.TOOL_PATH + LANCE_JAR"));

    if (DOCKER_REQUIRED) {
      //docker
      this.sourcesList.add(new DownloadImpl(
          "sudo wget " + UnixInstaller.DOCKER_RETRY_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
              + UnixInstaller.DOCKER_RETRY_INSTALL, UnixInstaller.TOOL_PATH
          + UnixInstaller.DOCKER_RETRY_INSTALL));
      this.sourcesList.add(new DownloadImpl(
          "sudo wget " + UnixInstaller.DOCKER_FIX_MTU_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
              + UnixInstaller.DOCKER_FIX_MTU_INSTALL, UnixInstaller.TOOL_PATH
          + UnixInstaller.DOCKER_FIX_MTU_INSTALL));
    }

    if (KAIROS_REQUIRED) {
      //kairosDB
      this.sourcesList.
          add(new DownloadImpl("sudo wget " + KAIROSDB_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
              + KAIROSDB_ARCHIVE, UnixInstaller.TOOL_PATH
              + KAIROSDB_ARCHIVE));
    }
    //visor
    this.sourcesList
        .add(new DownloadImpl(
            "sudo wget " + VISOR_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH + VISOR_JAR,
            UnixInstaller.TOOL_PATH + VISOR_JAR));

  }


  @Override
  public void installJava() throws RemoteException {

    LOGGER.debug(String.format("Starting Java installation on node %s", node.getId()));
    //create directory
    this.remoteConnection.executeCommand("sudo mkdir " + TOOL_PATH + JAVA_DIR);
    //extract java
    this.remoteConnection.executeCommand(
        "sudo tar zxvf " + TOOL_PATH + UnixInstaller.JAVA_ARCHIVE + " -C " + UnixInstaller.TOOL_PATH
            + JAVA_DIR
            + " --strip-components=1");
    // do not set symbolic link or PATH as there might be other Java versions on the VM

    LOGGER.debug(String.format("Java was successfully installed on node %s", node.getId()));
  }

  @Override
  public void installVisor() throws RemoteException {

    LOGGER.debug(String.format("Setting up Visor on node %s", node.getId()));
    //create properties file
    this.remoteConnection.writeFile("/tmp/" + VISOR_PROPERTIES,
        this.buildDefaultVisorConfig(), false);

    //move to tool path
    this.remoteConnection.executeCommand(
        "sudo mv " + "/tmp/" + VISOR_PROPERTIES + " " + TOOL_PATH + VISOR_PROPERTIES);

    //start visor

    String startCommand =
        "sudo nohup bash -c '" + this.JAVA_BINARY + " -jar " + TOOL_PATH + VISOR_JAR
            + " -conf " + TOOL_PATH + VISOR_PROPERTIES + " &> /dev/null &'";

    LOGGER.debug("Visor start command: " + startCommand);
    this.remoteConnection.executeCommand(startCommand);
    LOGGER.debug(String.format("Visor started successfully on node %s", node.getId()));
  }

  @Override
  public void installKairosDb() throws RemoteException {

    if (KAIROS_REQUIRED) {

      LOGGER
          .debug(String.format("Installing and starting KairosDB on node %s", node.getId()));
      this.remoteConnection.executeCommand("sudo mkdir " + KAIRROSDB_DIR);

      this.remoteConnection.executeCommand(
          "sudo tar  zxvf " + KAIROSDB_ARCHIVE + " -C " + KAIRROSDB_DIR
              + " --strip-components=1");

      this.remoteConnection.executeCommand(
          " sudo su -c \"(export PATH=\"" + UnixInstaller.TOOL_PATH + "/jre8/bin/:\"$PATH;nohup "
              + KAIRROSDB_DIR + "/bin/kairosdb.sh start)\"");

      LOGGER.debug(String.format("KairosDB started successfully on node %s", node.getId()));
    }
  }

  @Override
  public void installLance() throws RemoteException {

    if (DOCKER_REQUIRED) {
      LOGGER.debug(
          String.format("Installing and starting Lance: Docker on node %s", node.getId()));

      this.remoteConnection
          .executeCommand("sudo chmod +x " + TOOL_PATH + UnixInstaller.DOCKER_RETRY_INSTALL);
      // Install docker via the retry script:
      this.remoteConnection.executeCommand(
          "sudo nohup ./" + TOOL_PATH + UnixInstaller.DOCKER_RETRY_INSTALL
              + " > docker_retry_install.out 2>&1");
      this.remoteConnection
          .executeCommand("sudo chmod +x " + TOOL_PATH + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
      this.remoteConnection.executeCommand(
          "sudo nohup ./" + TOOL_PATH + UnixInstaller.DOCKER_FIX_MTU_INSTALL
              + " > docker_mtu_fix.out 2>&1");
      this.remoteConnection.executeCommand(
          "sudo nohup bash -c 'service docker restart' > docker_start.out 2>&1 ");

    }

    if(this.checkIfToolIsRunning(LANCE_JAR)){

      LOGGER.debug("Lance already running on node %s, skipping starting Lance", node.getId());

    }else{
      LOGGER.debug(String.format("Starting Lance on node %s", node.getId()));
      //start Lance
      String startCommand =
          "sudo nohup bash -c '" + this.JAVA_BINARY + " " + " -Dhost.ip.public=" + node
              .getIpAddressesList().stream().filter(p -> p.getType() == IpAddressType.PUBLIC_IP)
              .findAny().get().getIp()
              + " -Dhost.ip.private=" +
              node.getIpAddressesList().stream().filter(p -> p.getType() == IpAddressType.PRIVATE_IP)
                  .findAny().get().getIp() + " -Djava.rmi.server.hostname="
              + node.getIpAddressesList().stream().filter(p -> p.getType() == IpAddressType.PUBLIC_IP)
              .findAny().get().getIp() + " -Dhost.vm.id="
              + this.node.getId() + " -Dhost.vm.cloud.tenant.id=" + this.node.getUserId()
              + " -Dhost.vm.cloud.id=dummyCloud" + " -DLOG_DIR=" + TOOL_PATH
              + " -jar " + TOOL_PATH + LANCE_JAR + " > lance.out 2>&1 &' > lance.out 2>&1";

      LOGGER.debug("Lance start command: " + startCommand);

      this.remoteConnection.executeCommand(startCommand);

      LOGGER.debug(
          String.format("Lance installed and started successfully on node %s", node.getId()));
    }




  }

  @Override
  public void installSnap() throws RemoteException {

    LOGGER.debug(String.format("Installing and starting Snap on node %s", node.getId()));

    //download snap
    this.remoteConnection
        .executeCommand("sudo curl -s " + SNAP_DOWNLOAD + " | sudo bash > snap_preinstall.out");

    //install snap
    this.remoteConnection.executeCommand(
        "sudo apt-get install -y snap-telemetry > snap_install.out");

    //start snap service
    if (node.getNodeProperties().getOperationSystem().getOperatingSystemVersion()
        .startsWith("15.10") ||
        node.getNodeProperties().getOperationSystem().getOperatingSystemVersion().startsWith("16")
        ||
        node.getNodeProperties().getOperationSystem().getOperatingSystemVersion()
            .startsWith("17")) {
      this.remoteConnection.executeCommand("systemctl snap-telemetry start");
    } else { // assume its 14.10 or earlier
      this.remoteConnection.executeCommand("service start snap-telemetry");
    }

    LOGGER.debug(
        String.format("Snap installed and started successfully on node %s", node.getId()));
  }

  @Override
  public boolean checkIfToolIsRunning(String toolBinary) throws RemoteException {

    String command = " [ `pgrep " + toolBinary + "` ] && exit 0  || exit 99";

    int exitCode = this.remoteConnection.executeCommand(command).getExitStatus();

    if(exitCode==0)return true;

    return false;
  }

  @Override
  public void installAll() throws RemoteException {

    LOGGER.debug(
        String.format("Starting installation of all tools on UNIX on node %s", node.getId()));

    this.initSources();

    this.initToolDirectory();

    this.downloadSources(CHECK_IF_SOURCE_EXIST_COMMAND);

    this.installJava();

    this.installLance();

    this.installKairosDb();

    this.installVisor();

    this.installSnap();

    this.installSnap();
  }
}

