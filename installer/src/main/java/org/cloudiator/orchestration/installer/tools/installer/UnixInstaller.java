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
    private static final String CLOUDIATOR_DIR =  "/opt/cloudiator/";
    private final String JAVA_BINARY = CLOUDIATOR_DIR + "/" + UnixInstaller.JAVA_DIR + "/bin/java";
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



        //start Lance
        this.remoteConnection.executeCommand(
            "nohup bash -c '" + this.JAVA_BINARY + " " + " -Dhost.ip.public=" + node.getIpAddressesList().stream().filter(p -> p.getType() == IpAddressType.PUBLIC_IP).findAny().get()
                 + " -Dhost.ip.private=" +
                     node.getIpAddressesList().stream().filter(p -> p.getType() == IpAddressType.PRIVATE_IP).findAny().get() + " -Djava.rmi.server.hostname="
                + node.getIpAddressesList().stream().filter(p -> p.getType() == IpAddressType.PUBLIC_IP).findAny().get() + " -Dhost.vm.id="
                + this.node.getId() + " -Dhost.vm.cloud.tenant.id=" + this.userId + " -Dhost.vm.cloud.id="
                + " -jar " + UnixInstaller.LANCE_JAR + " > lance.out 2>&1 &' > lance.out 2>&1");

        LOGGER.debug(
            String.format("Lance installed and started successfully on node %s", node.getId()));
    }

    @Override public void installSnap() throws RemoteException {

        LOGGER.debug(String.format("Installing and starting Snap on node %s", node.getId()));

        //download snap
        this.remoteConnection.executeCommand("curl -s " + SNAP_DOWNLOAD + " | sudo bash > snap_preinstall.out" );

        //install snap
        this.remoteConnection.executeCommand(
            "sudo apt-get install -y snap-telemetry > snap_install.out");

        //start snap service
        if(node.getNodeProperties().getOperationSystem().getOperatingSystemVersion().startsWith("15.10") ||
            node.getNodeProperties().getOperationSystem().getOperatingSystemVersion().startsWith("16") ||
        node.getNodeProperties().getOperationSystem().getOperatingSystemVersion().startsWith("17")){
            this.remoteConnection.executeCommand("systemctl snap-telemetry start");
        } else { // assume its 14.10 or earlier
            this.remoteConnection.executeCommand("service start snap-telemetry");
        }

        LOGGER.debug(
            String.format("Snap installed and started successfully on node %s", node.getId()));
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

        this.installSnap();
        
        this.installSnap();
    }
}

