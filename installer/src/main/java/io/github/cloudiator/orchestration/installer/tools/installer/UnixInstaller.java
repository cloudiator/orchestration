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

import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.domain.Node;
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

    CommandTask bootstrap = new CommandTask(this.remoteConnection,"sudo mkdir " + UnixInstaller.TOOL_PATH);
    bootstrap.call();

    LOGGER.debug(String.format("Starting Java installation on node %s", node.id()));
    bootstrap = new CommandTask(this.remoteConnection, "sudo wget "
        + Configuration.conf().getString("installer.java.download") + "  -O " + UnixInstaller.TOOL_PATH
        + UnixInstaller.JAVA_ARCHIVE );
    bootstrap.call();
    //create directory
    bootstrap = new CommandTask(this.remoteConnection, "sudo mkdir " + TOOL_PATH + JAVA_DIR);
    bootstrap.call();
    //extract java
    // do not set symbolic link or PATH as there might be other Java versions on the VM
    bootstrap = new CommandTask(this.remoteConnection, "sudo tar zxvf " + TOOL_PATH + UnixInstaller.JAVA_ARCHIVE + " -C " + UnixInstaller.TOOL_PATH
        + JAVA_DIR
        + " --strip-components=1");
    bootstrap.call();

    LOGGER.debug(String.format("Java was successfully installed on node %s", node.id()));

  }


  @Override
  public void installVisor() throws RemoteException {

    //download Visor
    CommandTask installVisor = new CommandTask(this.remoteConnection,"sudo wget " + Configuration.conf().getString("installer.visor.download")
        + "  -O " + UnixInstaller.TOOL_PATH + VISOR_JAR);
    installVisor.call();

    LOGGER.debug(String.format("Setting up Visor on node %s", node.id()));
    //create properties file
    FileTask visorConfig = new FileTask(this.remoteConnection, "/tmp/" + VISOR_PROPERTIES,
        this.buildDefaultVisorConfig(), false);
    visorConfig.call();

    //move to tool path
    installVisor = new CommandTask(this.remoteConnection,"sudo mv " + "/tmp/" + VISOR_PROPERTIES + " " + TOOL_PATH + VISOR_PROPERTIES);
    installVisor.call();

    //start visor
    String startCommand =
        "sudo nohup bash -c '" + this.JAVA_BINARY + " -jar " + TOOL_PATH + VISOR_JAR
            + " -conf " + TOOL_PATH + VISOR_PROPERTIES + " &> /dev/null &'";
    LOGGER.debug("Visor start command: " + startCommand);
    installVisor = new CommandTask(this.remoteConnection,startCommand);
    installVisor.call();

    LOGGER.debug(String.format("Visor started successfully on node %s", node.id()));
  }

  @Override
  public void installKairosDb() throws RemoteException {

      //download KairosDB
      this.remoteConnection.executeCommand("sudo wget " +
          Configuration.conf().getString("installer.kairosdb.download") + "  -O " + UnixInstaller.TOOL_PATH
        + KAIROSDB_ARCHIVE);

      LOGGER
          .debug(String.format("Installing and starting KairosDB on node %s", node.id()));
      this.remoteConnection.executeCommand("sudo mkdir " + KAIRROSDB_DIR);

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
    CommandTask installLance = new CommandTask(this.remoteConnection,"sudo wget "
        + Configuration.conf().getString("installer.lance.download")
        + "  -O " + UnixInstaller.TOOL_PATH + LANCE_JAR);
    installLance.call();


    //start Lance
    String startCommand =
        "sudo nohup bash -c '" + this.JAVA_BINARY + " " + " -Dhost.ip.public=" + node
            .ipAddresses().stream().filter(p -> p.type() == IpAddress.IpAddressType.PUBLIC)
            .findAny().get().ip()
            + " -Dhost.ip.private=" +
            node.ipAddresses().stream().filter(p -> p.type() == IpAddress.IpAddressType.PRIVATE)
                .findAny().get().ip() + " -Djava.rmi.server.hostname="
            + node.ipAddresses().stream().filter(p -> p.type() == IpAddress.IpAddressType.PUBLIC)
            .findAny().get().ip() + " -Dhost.vm.id="
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
    CommandTask installDocker = new CommandTask(this.remoteConnection,"sudo wget " +
        Configuration.conf().getString("installer.docker.install.download") + "  -O " + UnixInstaller.TOOL_PATH
        + UnixInstaller.DOCKER_RETRY_INSTALL);
    installDocker.call();

    //download Docker fix MTU
    installDocker = new CommandTask(this.remoteConnection,"sudo wget " +
        Configuration.conf().getString("installer.docker.mtu.download") + "  -O " + UnixInstaller.TOOL_PATH
        + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
    installDocker.call();

    LOGGER.debug(
        String.format("Installing and starting Lance: Docker on node %s", node.id()));

    installDocker = new CommandTask(this.remoteConnection,"sudo chmod +x " + TOOL_PATH + UnixInstaller.DOCKER_RETRY_INSTALL);
    installDocker.call();

    // Install docker via the retry script:
    installDocker = new CommandTask(this.remoteConnection,"sudo nohup " + TOOL_PATH + UnixInstaller.DOCKER_RETRY_INSTALL
        + " > docker_retry_install.out 2>&1");
    installDocker.call();

    installDocker = new CommandTask(this.remoteConnection,"sudo chmod +x " + TOOL_PATH + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
    installDocker.call();

    installDocker = new CommandTask(this.remoteConnection,"sudo nohup " + TOOL_PATH + UnixInstaller.DOCKER_FIX_MTU_INSTALL
        + " > docker_mtu_fix.out 2>&1");
    installDocker.call();

    installDocker = new CommandTask(this.remoteConnection,"sudo nohup bash -c 'service docker restart' > docker_start.out 2>&1 ");
    installDocker.call();


    LOGGER.debug(String.format("Installing and starting Lance on node %s", node.id()));

  }
}

