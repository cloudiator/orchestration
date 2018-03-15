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
  private final String JAVA_BINARY = UnixInstaller.TOOL_PATH + JAVA_DIR + "/bin/java";


  public UnixInstaller(RemoteConnection remoteConnection, Node node, String userId) {
    super(remoteConnection, node, userId);

  }

  public void initToolDirectory() {

    try {

      LOGGER.debug(String.format(
          "Creating cloudiator tool directory in " + UnixInstaller.TOOL_PATH + " for node %s",
          node.id()));

      this.remoteConnection.executeCommand("sudo mkdir " + UnixInstaller.TOOL_PATH);


    } catch (RemoteException e) {
      e.printStackTrace();
    }

  }


  @Override
  public void initSources() {

    //java
    this.sourcesList
        .add("sudo wget " + UnixInstaller.JAVA_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
            + UnixInstaller.JAVA_ARCHIVE);
    //lance
    this.sourcesList
        .add("sudo wget " + LANCE_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH + LANCE_JAR);

    if (DOCKER_REQUIRED) {
      //docker
      this.sourcesList.add(
          "sudo wget " + UnixInstaller.DOCKER_RETRY_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
              + UnixInstaller.DOCKER_RETRY_INSTALL);
      this.sourcesList.add(
          "sudo wget " + UnixInstaller.DOCKER_FIX_MTU_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
              + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
    }

    if (KAIROS_REQUIRED) {
      //kairosDB
      this.sourcesList.
          add("sudo wget " + KAIROSDB_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH
              + KAIROSDB_ARCHIVE);
    }
    //visor
    this.sourcesList
        .add("sudo wget " + VISOR_DOWNLOAD + "  -O " + UnixInstaller.TOOL_PATH + VISOR_JAR);

  }


  @Override
  public void installJava() throws RemoteException {

    LOGGER.debug(String.format("Starting Java installation on node %s", node.id()));
    //create directory
    this.remoteConnection.executeCommand("sudo mkdir " + TOOL_PATH + JAVA_DIR);
    //extract java
    this.remoteConnection.executeCommand(
        "sudo tar zxvf " + TOOL_PATH + UnixInstaller.JAVA_ARCHIVE + " -C " + UnixInstaller.TOOL_PATH
            + JAVA_DIR
            + " --strip-components=1");
    // do not set symbolic link or PATH as there might be other Java versions on the VM

    LOGGER.debug(String.format("Java was successfully installed on node %s", node.id()));
  }

  @Override
  public void installVisor() throws RemoteException {

    LOGGER.debug(String.format("Setting up Visor on node %s", node.id()));
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
    LOGGER.debug(String.format("Visor started successfully on node %s", node.id()));
  }

  @Override
  public void installKairosDb() throws RemoteException {

    if (KAIROS_REQUIRED) {

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
  }

  @Override
  public void installLance() throws RemoteException {

    if (DOCKER_REQUIRED) {
      LOGGER.debug(
          String.format("Installing and starting Lance: Docker on node %s", node.id()));

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
    LOGGER.debug(String.format("Installing and starting Lance on node %s", node.id()));

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

    this.remoteConnection.executeCommand(startCommand);

    LOGGER.debug(
        String.format("Lance installed and started successfully on node %s", node.id()));
  }



  @Override
  public void installAll() throws RemoteException {

    LOGGER.debug(
        String.format("Starting installation of all tools on UNIX on node %s", node.id()));

    this.initSources();

    this.initToolDirectory();

    this.downloadSources();

    this.installJava();

    this.installLance();

    this.installKairosDb();

    this.installVisor();


  }
}

