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


import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.orchestration.installer.tools.Download;
import io.github.cloudiator.orchestration.installer.tools.installer.api.InstallApi;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.cloudiator.messages.NodeEntities.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * todo clean up class, do better logging
 */
abstract class AbstractInstaller implements InstallApi {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractInstaller.class);


  protected final RemoteConnection remoteConnection;


  protected final List<Download> sourcesList = new ArrayList<>();

  //TODO: refactor, replace usage of Play config file
  //parallel download threads
  private static final int NUMBER_OF_DOWNLOAD_THREADS = 1;
  //Play.application().configuration().getInt("colosseum.installer.download.threads");

  //KairosDBom
  protected static final String KAIROSDB_ARCHIVE = "kairosdb.tar.gz";
  protected static final String KAIRROSDB_DIR = "kairosdb";
  protected static final String KAIROSDB_DOWNLOAD = "https://github.com/kairosdb/kairosdb/releases/download/v0.9.4/kairosdb-0.9.4-6.tar.gz";
  //Play.application().configuration()
  //  .getString("colosseum.installer.abstract.kairosdb.download");

  //Visor
  protected static final String VISOR_JAR = "visor.jar";
  protected static final String VISOR_DOWNLOAD = "https://omi-dev.e-technik.uni-ulm.de/jenkins/job/cloudiator-visor/lastSuccessfulBuild/artifact/visor-service/target/visor.jar";
  //Play.application().configuration().getString("colosseum.installer.abstract.visor.download");

  //Lance
  protected static final String LANCE_JAR = "lance.jar";
  protected static final String LANCE_DOWNLOAD = "https://omi-dev.e-technik.uni-ulm.de/jenkins/job/cloudiator-lance/lastSuccessfulBuild/artifact/server/target/lance-server-jar-with-dependencies.jar";
  //Play.application().configuration().getString("colosseum.installer.abstract.lance.download");


  //Java
  protected static final String JAVA_DIR = "jre8";


  protected static final String VISOR_PROPERTIES = "default.properties";

  protected final Node node;


  public AbstractInstaller(RemoteConnection remoteConnection, Node node) {

    checkNotNull(remoteConnection);

    this.remoteConnection = remoteConnection;

    this.node = node;

  }

  @Override
  public void downloadSources(String checkIfExistsCommand) {

    LOGGER.debug("Start downloading sources...");
    ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_DOWNLOAD_THREADS);

    List<Callable<Integer>> tasks = new ArrayList<>();

    for (final Download download : this.sourcesList) {

      //check if source exist
      if(checkIfSourceExists(checkIfExistsCommand, download.filePath())){
        LOGGER.debug("Source: " + download + " already exists, skipping download");
      }else{
        //if source does not exist add to download list
        Callable<Integer> downloadTask =
            new DownloadTask(this.remoteConnection, download.downloadCommand());
        tasks.add(downloadTask);
      }


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

  private boolean checkIfSourceExists(String checkIfExistsCommand, String filePath){

    String command = checkIfExistsCommand.replace("?", filePath);

    try {
      int exitCode = this.remoteConnection.executeCommand(command).getExitStatus();

      if(exitCode == 0){
        return true;
      }
    } catch (RemoteException e) {
      LOGGER.error("Error while checking if source already exists!", e);
    }

    return false;

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
        + "chukwaUrl = " + "http://localhost:8080/chukwa" + "\n" +
        "chukwaVmId = " + "dummyNodeId";

        /*
        return "executionThreads = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.executionThreads") +
            "\n" + "reportingInterval = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.reportingInterval") +
            "\n" + "telnetPort = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.telnetPort") + "\n" +
            "restHost = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.restHost") + "\n" +
            "restPort = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.restPort") + "\n" +
            "kairosServer = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.kairosServer") + "\n" +
            "kairosPort = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.kairosPort") + "\n" +
            "reportingModule = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.reportingModule") +
            "\n" + "chukwaUrl = " + Play.application().configuration()
            .getString("colosseum.installer.abstract.visor.config.chukwaUrl") + "\n" +
            "chukwaVmId = " + virtualMachine.providerId().get();
            */

    return config;

  }

  @Override
  public void close() {
    LOGGER.info("Installation of all tools finished, closing remote connection!");
    this.remoteConnection.close();
  }
}

