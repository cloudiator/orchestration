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

package org.cloudiator.orchestration.installer.tools.installer;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import org.cloudiator.messages.NodeOuterClass.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * todo clean up class, do better logging
 */
public class UnixInstaller extends AbstractInstaller {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(UnixInstaller.class);


    private static final String DOCKER_FIX_MTU_INSTALL = "docker_fix_mtu.sh";
    private static final String CLOUDIATOR_DIR =  "/opt/cloudiator/";
    private final String JAVA_BINARY = CLOUDIATOR_DIR + "/" + UnixInstaller.JAVA_DIR + "/bin/java";
    private static final String JAVA_ARCHIVE = "jre8.tar.gz";
    private static final String JAVA_DOWNLOAD = "";
        //Play.application().configuration().getString("colosseum.installer.linux.java.download");
    private static final String DOCKER_RETRY_DOWNLOAD = "";
            //Play.application().configuration()
        //.getString("colosseum.installer.linux.lance.docker_retry.download");
    private static final String DOCKER_FIX_MTU_DOWNLOAD = "";
    //Play.application().configuration()
      //  .getString("colosseum.installer.linux.lance.docker.mtu.download");
    private static final String DOCKER_RETRY_INSTALL = "docker_retry.sh";
    private static final boolean KAIROS_REQUIRED = false;
    //Play.application().configuration()
      //  .getBoolean("colosseum.installer.linux.kairosdb.install.flag");
    private static final boolean DOCKER_REQUIRED = false;
    //Play.application().configuration()
      //  .getBoolean("colosseum.installer.linux.lance.docker.install.flag");


    private static final String toolPath = "/opt/cloudiator/";

    public UnixInstaller(RemoteConnection remoteConnection, Node node, String userId) {
        super(remoteConnection, node, userId);



    }

    @Override public void initSources() {

        //java
        this.sourcesList
            .add("wget " + UnixInstaller.JAVA_DOWNLOAD + "  -O " + UnixInstaller.JAVA_ARCHIVE);
        //lance
        this.sourcesList
            .add("wget " + UnixInstaller.LANCE_DOWNLOAD + "  -O " + UnixInstaller.LANCE_JAR);

        if (DOCKER_REQUIRED) {
            //docker
            this.sourcesList.add("wget " + UnixInstaller.DOCKER_RETRY_DOWNLOAD + "  -O "
                + UnixInstaller.DOCKER_RETRY_INSTALL);
            this.sourcesList.add("wget " + UnixInstaller.DOCKER_FIX_MTU_DOWNLOAD + "  -O "
                + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
        }

        if (KAIROS_REQUIRED) {
            //kairosDB
            this.sourcesList.
                add("wget " + UnixInstaller.KAIROSDB_DOWNLOAD + "  -O "
                    + UnixInstaller.KAIROSDB_ARCHIVE);
        }
        //visor
        this.sourcesList
            .add("wget " + UnixInstaller.VISOR_DOWNLOAD + "  -O " + UnixInstaller.VISOR_JAR);

    }



    @Override public void installJava() throws RemoteException {

        LOGGER.debug(String.format("Starting Java installation on node %s", node.getId()));
        //create directory
        this.remoteConnection.executeCommand("mkdir " + UnixInstaller.JAVA_DIR);
        //extract java
        this.remoteConnection.executeCommand(
            "tar zxvf " + UnixInstaller.JAVA_ARCHIVE + " -C " + UnixInstaller.JAVA_DIR
                + " --strip-components=1");
        // do not set symbolic link or PATH as there might be other Java versions on the VM

        LOGGER.debug(String.format("Java was successfully installed on node %s", node.getId()));
    }

    @Override public void installVisor() throws RemoteException {

        LOGGER.debug(String.format("Setting up Visor on node %s", node.getId()));
        //create properties file
        this.remoteConnection.writeFile(this.CLOUDIATOR_DIR + "/" + UnixInstaller.VISOR_PROPERTIES,
            this.buildDefaultVisorConfig(), false);

        //start visor
        this.remoteConnection.executeCommand(
            "sudo nohup bash -c '" + this.JAVA_BINARY + " -jar " + UnixInstaller.VISOR_JAR
                + " -conf " + UnixInstaller.VISOR_PROPERTIES + " &> /dev/null &'");
        LOGGER.debug(String.format("Visor started successfully on node %s", node.getId()));
    }

    @Override public void installKairosDb() throws RemoteException {

        if (KAIROS_REQUIRED) {

            LOGGER
                .debug(String.format("Installing and starting KairosDB on node %s", node.getId()));
            this.remoteConnection.executeCommand("mkdir " + UnixInstaller.KAIRROSDB_DIR);

            this.remoteConnection.executeCommand(
                "tar  zxvf " + UnixInstaller.KAIROSDB_ARCHIVE + " -C " + UnixInstaller.KAIRROSDB_DIR
                    + " --strip-components=1");

            this.remoteConnection.executeCommand(
                " sudo su -c \"(export PATH=\"" + this.CLOUDIATOR_DIR + "/jre8/bin/:\"$PATH;nohup "
                    + UnixInstaller.KAIRROSDB_DIR + "/bin/kairosdb.sh start)\"");

            LOGGER.debug(String.format("KairosDB started successfully on node %s", node.getId()));
        }
    }

    @Override public void installLance() throws RemoteException {

        if (DOCKER_REQUIRED) {
            LOGGER.debug(
                String.format("Installing and starting Lance: Docker on node %s", node.getId()));

            this.remoteConnection
                .executeCommand("sudo chmod +x " + UnixInstaller.DOCKER_RETRY_INSTALL);
            // Install docker via the retry script:
            this.remoteConnection.executeCommand(
                "sudo nohup ./" + UnixInstaller.DOCKER_RETRY_INSTALL
                    + " > docker_retry_install.out 2>&1");
            this.remoteConnection
                .executeCommand("sudo chmod +x " + UnixInstaller.DOCKER_FIX_MTU_INSTALL);
            this.remoteConnection.executeCommand(
                "sudo nohup ./" + UnixInstaller.DOCKER_FIX_MTU_INSTALL
                    + " > docker_mtu_fix.out 2>&1");
            this.remoteConnection.executeCommand(
                "sudo nohup bash -c 'service docker restart' > docker_start.out 2>&1 ");

        }
        LOGGER.debug(String.format("Installing and starting Lance on node %s", node.getId()));


        node.getIpAddressesList()
        //start Lance
        this.remoteConnection.executeCommand(
            "nohup bash -c '" + this.JAVA_BINARY + " " + " -Dhost.ip.public=" + this.node.getIpAddressesList().stream().findAny().publicAddresses().stream().findAny().get()
                 + " -Dhost.ip.private=" +
                this.virtualMachine.privateAddresses().stream().findAny().get() + " -Djava.rmi.server.hostname="
                + this.virtualMachine.publicAddresses().stream().findAny().get() + " -Dhost.vm.id="
                + this.virtualMachine.id() + " -Dhost.vm.cloud.tenant.id=" + this.tenant.getId() + " -Dhost.vm.cloud.id="
                //TODO: how to get cloud id of VM? How about node cloud relation? + this.virtualMachine.cloud().getUuid()
                + " -jar " + UnixInstaller.LANCE_JAR + " > lance.out 2>&1 &' > lance.out 2>&1");

        LOGGER.debug(
            String.format("Lance installed and started successfully on node %s", node.getId()));
    }

    @Override public void installAll() throws RemoteException {

        LOGGER.debug(
            String.format("Starting installation of all tools on UNIX on node %s", node.getId()));

        this.initSources();
        this.downloadSources();

        this.installJava();

        this.installLance();

        this.installKairosDb();

        this.installVisor();
    }
}

