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


import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.orchestration.installer.tools.installer.api.InstallApi;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * todo clean up class, do better logging
 */
abstract class AbstractInstaller implements InstallApi {

  //KairosDBom
  protected static final String KAIROSDB_ARCHIVE = "kairosdb.tar.gz";
  protected static final String KAIRROSDB_DIR = "kairosdb";
  protected static final String KAIROSDB_DOWNLOAD = "https://github.com/kairosdb/kairosdb/releases/download/v0.9.4/kairosdb-0.9.4-6.tar.gz";
  //Visor
  protected static final String VISOR_JAR = "visor.jar";
  //Play.application().configuration().getInt("colosseum.installer.download.threads");
  protected static final String VISOR_DOWNLOAD = "https://oss.sonatype.org/content/repositories/snapshots/io/github/cloudiator/visor/visor-service/0.3.0-SNAPSHOT/visor-service-0.3.0-20180219.105415-1.jar";
  //Lance
  protected static final String LANCE_JAR = "lance.jar";
  protected static final String LANCE_DOWNLOAD = "https://oss.sonatype.org/content/repositories/snapshots/io/github/cloudiator/lance/server/0.2.0-SNAPSHOT/server-0.2.0-20171122.122528-54-jar-with-dependencies.jar";
  //Play.application().configuration()
  //  .getString("colosseum.installer.abstract.kairosdb.download");
  //Java
  protected static final String JAVA_DIR = "jre8";
  protected static final String VISOR_PROPERTIES = "default.properties";
  //Play.application().configuration().getString("colosseum.installer.abstract.visor.download");
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractInstaller.class);
  //TODO: refactor, replace usage of Play config file
  //parallel download threads
  private static final int NUMBER_OF_DOWNLOAD_THREADS = 1;
  //Play.application().configuration().getString("colosseum.installer.abstract.lance.download");
  protected final RemoteConnection remoteConnection;
  protected final List<String> sourcesList = new ArrayList<>();
  protected final Node node;

  //TODO: might be changed to Tenant
  protected final String userId;
  
  
  // Alluxio and DLMS Agent
  protected static final String ALLUXIO_DIR = "alluxio";
  protected static final String ALLUXIO_DOWNLOAD = "http://downloads.alluxio.org/downloads/files/1.8.1/alluxio-1.8.1-bin.tar.gz";
  protected static final String ALLUXIO_ARCHIVE = "alluxio.tar.gz";
  
  protected static final String DLMS_AGENT_JAR = "dlmsagent.jar";

  

  public AbstractInstaller(RemoteConnection remoteConnection, Node node, String userId) {

    checkNotNull(remoteConnection);

    this.remoteConnection = remoteConnection;

    this.node = node;
    this.userId = userId;

  }

  public void downloadSources() {

    LOGGER.debug("Start downloading sources...");
    ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_DOWNLOAD_THREADS);

    List<Callable<Integer>> tasks = new ArrayList<>();

    for (final String downloadCommand : this.sourcesList) {

      Callable<Integer> downloadTask =
          new DownloadTask(this.remoteConnection, downloadCommand);
      tasks.add(downloadTask);
    }
    try {
      List<Future<Integer>> results = executorService.invokeAll(tasks);

      for (Future<Integer> exitCode : results) {
        if (exitCode.get() != 0) {
          throw new RuntimeException("Downloading of one or more sources failed!");
        }
      }
      LOGGER.debug("All sources downloaded successfully!");
      executorService.shutdown();
    } catch (InterruptedException e) {
      LOGGER.error("Installer: Interrupted Exception while downloading sources!", e);
    } catch (ExecutionException e) {
      LOGGER.error("Installer: Execution Exception while downloading sources!", e);
    }

  }

  protected String buildDefaultVisorConfig() {

    String config = "executionThreads = " + 20 + "\n"
        + "reportingInterval = " + 10 + "\n"
        + "telnetPort = " + 9001 + "\n"
        + "restHost = " + "http://0.0.0.0" + "\n" +
        "restPort = " + 31415 + "\n" +
        "kairosServer = " + "localhost" + "\n" +
        "kairosPort = " + 8080 + "\n" +
        "reportingModule = "
        + "de.uniulm.omi.cloudiator.visor.reporting.kairos.KairosReportingModule" + "\n"
        + "chukwaUrl = " + "http://localhost:8080/chukwa" + "\n"
        + "chukwaVmId = " + "dummyNodeId" + "\n"
        + "influxUrl =" + "" + "\n"
        + "influxDatabaseName=visor" + "\n"
        + "influxUserName=root" + "\n"
        + "influxPassword=root" + "\n"
        + "jsonTcpServer=localhost" + "\n"
        + "jsonTcpPort=5000" + "\n"
        + "vmId=" + this.node.id() + "\n"
        + "cloudId=1" + "\n"
        + "jmsBroker=tcp://localhost:61616";

    return config;

  }

  @Override
  public void close() {
    LOGGER.info("Installation of all tools finished, closing remote connection!");
    this.remoteConnection.close();
  }
}


