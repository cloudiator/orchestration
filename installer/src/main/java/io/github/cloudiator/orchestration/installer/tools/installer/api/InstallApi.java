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

package io.github.cloudiator.orchestration.installer.tools.installer.api;


import de.uniulm.omi.cloudiator.sword.remote.RemoteException;

/**
 * Created by Daniel Seybold on 19.05.2015.
 */
public interface InstallApi extends AutoCloseable {

  /**
   * Creates required folders and installs required software
   */
  void bootstrap() throws RemoteException;


  /**
   * create the visor configuration file and start visor
   */
  void installVisor() throws RemoteException;

  /**
   * extract and start kairosDB
   */
  void installKairosDb() throws RemoteException;

  /**
   * download, setup and start Lance (LifecycleAgent - Unix:Docker, Windows: not yet decided)
   */
  void installLance() throws RemoteException;


  /**
   * Download and install Docker
   */
  void installDocker() throws RemoteException;

  /**

   * Download and install Alluxio
   */
  void installAlluxio() throws RemoteException;
  
  /**
   * Download and install DLMSAgent
   */
  void installDlmsAgent() throws RemoteException;

   * Fetch and start Cloudiator Spark Worker container
   */
  void installSparkWorker() throws RemoteException;

  /**
   * Fetch and start EMS
   */
  void installEMS() throws RemoteException;


  @Override
  void close() throws RemoteException;
}


