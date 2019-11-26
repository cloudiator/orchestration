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

/**
 * Created by Daniel Seybold on 17.05.2018.
 */

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 19.05.2015.
 */
public class CommandTask implements Callable<Integer> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DownloadTask.class);

  private final RemoteConnection remoteConnection;
  private final String command;

  public CommandTask(RemoteConnection remoteConnection, String command) {
    this.remoteConnection = remoteConnection;
    this.command = command;

  }

  @Override
  public Integer call() throws RemoteException {
    LOGGER.debug("Executing command: " + this.command);

    Integer exitCode = this.remoteConnection.executeCommand(this.command).getExitStatus();

    if (exitCode.intValue() != 0) {
      throw new RemoteException("Execution of command: " + command + " failed! Exitcode: "+ exitCode);
    }

    return exitCode;
  }
}
