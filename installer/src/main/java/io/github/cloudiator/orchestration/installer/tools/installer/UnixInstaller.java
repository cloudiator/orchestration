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

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnectionResponse;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.domain.Node;
import org.apache.commons.lang3.StringUtils;
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
  private static final String JAVA_ARCHIVE = "jre8.tar.gz";
  private static final String DOCKER_RETRY_INSTALL = "docker_retry.sh";
  private final String JAVA_BINARY = UnixInstaller.TOOL_PATH + JAVA_DIR + "/bin/java";


  public UnixInstaller(RemoteConnection remoteConnection, Node node, String userId) {
    super(remoteConnection, node, userId);

  }


  @Override
  public void bootstrap() throws RemoteException {

    //create Cloudiator directory
    LOGGER.debug(String.format(
        "Creating Cloudiator tool directory in " + UnixInstaller.TOOL_PATH + " for node %s",
        node.id()));

    CommandTask bootstrap = new CommandTask(this.remoteConnection,
        "sudo mkdir -p " + UnixInstaller.TOOL_PATH);
    bootstrap.call();

    LOGGER.debug(String.format("Starting Java installation on node %s", node.id()));
    bootstrap = new CommandTask(this.remoteConnection, "sudo wget "
        + Configuration.conf().getString("installer.java.download") + "  -O "
        + UnixInstaller.TOOL_PATH
        + UnixInstaller.JAVA_ARCHIVE);
    bootstrap.call();
    //create directory
    bootstrap = new CommandTask(this.remoteConnection, "sudo mkdir -p " + TOOL_PATH + JAVA_DIR);
    bootstrap.call();
    //extract java
    // do not set symbolic link or PATH as there might be other Java versions on the VM
    bootstrap = new CommandTask(this.remoteConnection,
        "sudo tar zxvf " + TOOL_PATH + UnixInstaller.JAVA_ARCHIVE + " -C " + UnixInstaller.TOOL_PATH
            + JAVA_DIR
            + " --strip-components=1");
    bootstrap.call();

    LOGGER.debug(String.format("Java was successfully installed on node %s", node.id()));

  }


  @Override
  public void installVisor() throws RemoteException {

    //check for installed Visor
    RemoteConnectionResponse checkresult = this.remoteConnection
        .executeCommand("ps -ef | grep -c \"[v]isor.jar\"");
    if (!checkresult.stdOut().matches("0")) {
      LOGGER.debug("Result: " + checkresult.stdOut() + " - VisorInstallation canceled ");
    } else {

      //download Visor
      CommandTask installVisor = new CommandTask(this.remoteConnection,
          "sudo wget " + Configuration.conf().getString("installer.visor.download")
              + "  -O " + UnixInstaller.TOOL_PATH + VISOR_JAR);
      installVisor.call();

      LOGGER.debug(String.format("Setting up Visor on node %s", node.id()));
      //create properties file
      FileTask visorConfig = new FileTask(this.remoteConnection, "/tmp/" + VISOR_PROPERTIES,
          this.buildDefaultVisorConfig(), false);
      visorConfig.call();

      //move to tool path
      installVisor = new CommandTask(this.remoteConnection,
          "sudo mv " + "/tmp/" + VISOR_PROPERTIES + " " + TOOL_PATH + VISOR_PROPERTIES);
      installVisor.call();

      //start visor
      String startCommand =
          "sudo nohup bash -c '" + this.JAVA_BINARY + " -jar " + TOOL_PATH + VISOR_JAR
              + " -conf " + TOOL_PATH + VISOR_PROPERTIES + " &> /dev/null &'";
      LOGGER.debug("Visor start command: " + startCommand);
      installVisor = new CommandTask(this.remoteConnection, startCommand);
      installVisor.call();

      LOGGER.debug(String.format("Visor started successfully on node %s", node.id()));
    }
  }

  @Override
  public void installKairosDb() throws RemoteException {

    //download KairosDB
    this.remoteConnection.executeCommand("sudo wget " +
        Configuration.conf().getString("installer.kairosdb.download") + "  -O "
        + UnixInstaller.TOOL_PATH
        + KAIROSDB_ARCHIVE);

    LOGGER
        .debug(String.format("Installing and starting KairosDB on node %s", node.id()));
    this.remoteConnection.executeCommand("sudo mkdir -p " + KAIRROSDB_DIR);

    this.remoteConnection.executeCommand(
        "sudo tar  zxvf " + KAIROSDB_ARCHIVE + " -C " + KAIRROSDB_DIR
            + " --strip-components=1");

    this.remoteConnection.executeCommand(
        " sudo su -c \"(export PATH=\"" + UnixInstaller.TOOL_PATH + "/jre8/bin/:\"$PATH;nohup "
            + KAIRROSDB_DIR + "/bin/kairosdb.sh start)\"");

    LOGGER.debug(String.format("KairosDB started successfully on node %s", node.id()));

  }

  @Override
  public void installLance() throws RemoteException {

    //download Lance
    CommandTask installLance = new CommandTask(this.remoteConnection, "sudo wget "
        + Configuration.conf().getString("installer.lance.download")
        + "  -O " + UnixInstaller.TOOL_PATH + LANCE_JAR);
    installLance.call();

    final String publicIpAddress = node.connectTo().ip();
    final String privateIpAddress = node.privateIpAddresses().stream().findAny()
        .orElse(node.connectTo()).ip();

    //start Lance
    String startCommand =
        "sudo nohup bash -c '" + this.JAVA_BINARY + " " + " -Dhost.ip.public=" + publicIpAddress
            + " -Dhost.ip.private=" +
            privateIpAddress + " -Djava.rmi.server.hostname="
            + publicIpAddress + " -Dhost.vm.id="
            + this.node.id() + " -Dhost.vm.cloud.tenant.id=" + this.userId
            + " -Dhost.vm.cloud.id=dummyCloud" + " -DLOG_DIR=" + TOOL_PATH
            + " -jar " + TOOL_PATH + LANCE_JAR + " > lance.out 2>&1 &' > lance.out 2>&1";
    LOGGER.debug("Lance start command: " + startCommand);

    installLance = new CommandTask(this.remoteConnection, startCommand);
    installLance.call();

    LOGGER.debug(
        String.format("Lance installed and started successfully on node %s", node.id()));
  }


  @Override
  public void installDocker() throws RemoteException {

    //download Docker install script
    CommandTask installDocker = new CommandTask(this.remoteConnection, "sudo wget " +
        Configuration.conf().getString("installer.docker.binary.download") + "  -O "
        + UnixInstaller.TOOL_PATH
        + UnixInstaller.DOCKER_RETRY_INSTALL);
    installDocker.call();

    //download Docker fix MTU
    installDocker = new CommandTask(this.remoteConnection, "sudo wget " +
        Configuration.conf().getString("installer.docker.mtu.download") + "  -O "
        + UnixInstaller.TOOL_PATH
        + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
    installDocker.call();

    LOGGER.debug(
        String.format("Installing and starting Lance: Docker on node %s", node.id()));

    installDocker = new CommandTask(this.remoteConnection,
        "sudo chmod +x " + TOOL_PATH + UnixInstaller.DOCKER_RETRY_INSTALL);
    installDocker.call();

    // Install docker via the retry script:
    installDocker = new CommandTask(this.remoteConnection,
        "sudo nohup " + TOOL_PATH + UnixInstaller.DOCKER_RETRY_INSTALL
            + " > docker_retry_install.out 2>&1");
    installDocker.call();

    installDocker = new CommandTask(this.remoteConnection,
        "sudo chmod +x " + TOOL_PATH + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
    installDocker.call();

    installDocker = new CommandTask(this.remoteConnection,
        "sudo nohup " + TOOL_PATH + UnixInstaller.DOCKER_FIX_MTU_INSTALL
            + " > docker_mtu_fix.out 2>&1");
    installDocker.call();

    installDocker = new CommandTask(this.remoteConnection,
        "sudo nohup bash -c 'service docker restart' > docker_start.out 2>&1 ");
    installDocker.call();

    LOGGER.debug(String.format("Installed Docker on node %s", node.id()));

  }

  @Override
  public void installSparkWorker() throws RemoteException {

    LOGGER.debug(
        String.format("Fixing hostname for Spark Workers on node %s", node.id()));
    CommandTask fixHostname = new CommandTask(this.remoteConnection, ""
        + "sudo rm /etc/hosts "
        + " && "
        + " echo 127.0.0.1 localhost.localdomain localhost `hostname` | sudo tee /etc/hosts");
    fixHostname.call();

    LOGGER.debug(
        String.format("Fetching and starting Spark Worker container on node %s", node.id()));

    //download Docker install script
    CommandTask startSparkWorkerContainer = new CommandTask(this.remoteConnection,
        "sudo docker run -d "
            + " -e SPARK_MASTER_ENDPOINT=" + Configuration.conf()
            .getString("installer.spark.master.ip")
            + " -e SPARK_MASTER_PORT=" + Configuration.conf()
            .getString("installer.spark.master.port")
            + " -e SPARK_WORKER_UI_PORT=" + Configuration.conf()
            .getString("installer.spark.worker.ui")
            + " -p 9999:9999 "
            + " -p " + Configuration.conf().getString("installer.spark.worker.ui") + ":"
            + Configuration.conf().getString("installer.spark.worker.ui")
            + " --network host "
            + " cloudiator/spark-worker:latest ");

    startSparkWorkerContainer.call();
    LOGGER
        .debug(String.format("Successfully started Spark Worker container  on node %s", node.id()));

  }

  @Override
  public void installEMS() throws RemoteException {

    // Print node information
    LOGGER.debug(String
        .format("Node information: id=%s, name=%s, type=%s", node.id(), node.name(), node.type()));
    LOGGER.debug(String.format("Node public addresses: %s", node.publicIpAddresses()));
    LOGGER.debug(String.format("Node 'connectTo' addresses: %s", node.connectTo()));

    // Prepare EMS url to invoke
    String emsUrl = Configuration.conf().getString("installer.ems.url");
    String emsApiKey = Configuration.conf().getString("installer.ems.api-key");

    if (StringUtils.isNotBlank(emsUrl)) {
      // Append API-key
      if (StringUtils.isNotBlank(emsApiKey)) {
        emsUrl = emsUrl + "?ems-api-key=" + emsApiKey;
      }
      LOGGER.debug(String.format("EMS Server url: %s", emsUrl));

      // Contact EMS to get EMS Client installation instructions for this node
      LOGGER.debug(String.format(
          "Contacting EMS Server to retrieve EMS Client installation info for node %s: url=%s",
          node.id(), emsUrl));
      InstallerHelper.InstallationInstructions installationInstructions = InstallerHelper
          .getInstallationInstructionsFromServer(node, emsUrl);
      LOGGER.debug(String.format("Installation instructions for node %s: %s", node.id(),
          installationInstructions));

      // Execute installation instructions
      LOGGER.debug(
          String.format("Executing EMS Client installation instructions on node %s", node.id()));
      InstallerHelper.executeInstructions(node, remoteConnection, installationInstructions);

      LOGGER.debug(String.format("EMS Client installation completed on node %s", node.id()));
    } else {
      LOGGER.warn(String.format("EMS Client installation is switched off"));
    }
  }
}

