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

package io.github.cloudiator.orchestration.installer;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.orchestration.installer.remote.CompositeRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.KeyPairRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.PasswordRemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.remote.RemoteConnectionStrategy;
import io.github.cloudiator.orchestration.installer.tools.installer.Installers;
import io.github.cloudiator.orchestration.installer.tools.installer.api.InstallApi;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Installation.InstallationResponse;
import org.cloudiator.messages.InstallationEntities.Tool;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 12.09.2017.
 */
public class InstallEventSubscriber implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(InstallEventSubscriber.class);
  private static final int SERVER_ERROR = 500;
  private final MessageInterface messagingService;
  private final NodeToNodeMessageConverter nodeToNodeMessageConverter = new NodeToNodeMessageConverter();


  @Inject
  public InstallEventSubscriber(MessageInterface messageInterface) {
    this.messagingService = messageInterface;
  }

  @Override
  public void run() {

    final Subscription subscribe = messagingService.subscribe(InstallationRequest.class,
        InstallationRequest.parser(), (requestId, InstallationRequest) -> {

          try {
            List<Tool> installedTools = handleRequest(requestId,
                InstallationRequest);
            sendInstallResponse(requestId, installedTools);
          } catch (Exception ex) {
            LOGGER.error("exception occurred.", ex);
            sendErrorResponse(requestId,
                "exception occurred: " + ex.getMessage(), SERVER_ERROR);
          }
        });

  }

  private final List<Tool> handleRequest(String requestId, InstallationRequest installationRequest)
      throws RemoteException {
    //TODO: implement queue + worker as done for NodeEvent to enable mutlipe installations in parallel

    LOGGER.debug("Received installRequest with requestId: " + requestId);

    Node node = nodeToNodeMessageConverter
        .applyBack(installationRequest.getInstallation().getNode());

    RemoteConnectionStrategy remoteConnectionStrategy = new CompositeRemoteConnectionStrategy(
        Sets.newHashSet(
            new PasswordRemoteConnectionStrategy(), new KeyPairRemoteConnectionStrategy()));

    List<Tool> installedTools;

    RemoteConnection remoteConnection = remoteConnectionStrategy.connect(node);

    installedTools = installTools(installationRequest.getInstallation().getToolList(),
        remoteConnection, node,
        installationRequest.getUserId());

    return installedTools;

  }


  public List<Tool> installTools(List<Tool> tools, RemoteConnection remoteConnection, Node node,
      String userId) throws RemoteException {

    LOGGER.debug("Remote connection established, starting to install Cloudiator tools...");

    List<Tool> installedTools = new ArrayList<>();

    InstallApi installApi = Installers.of(remoteConnection, node, userId);

    //TODO: replace default Java installation when integrating Ansible scripts
    installApi.bootstrap();

    for (Tool tool : tools) {
      if (tool.equals(Tool.LANCE)) {
        installApi.installLance();
        installedTools.add(tool);
      } else if (tool.equals(Tool.AXE)) {
        LOGGER.warn("AXE installation currently supported!");
      } else if (tool.equals(Tool.DOCKER)) {
        installApi.installDocker();
        installedTools.add(tool);
      } else if (tool.equals(Tool.KAIROSDB)) {
        installApi.installKairosDb();
        installedTools.add(tool);
      } else if (tool.equals(Tool.VISOR)) {
        installApi.installVisor();
        installedTools.add(tool);
      } else if (tool.equals(Tool.SPARK_WORKER)) {
        installApi.installSparkWorker();
        installedTools.add(tool);
      } else {
        throw new IllegalStateException("Unsupported toolName: " + tool.name());
      }
    }

    return installedTools;

  }


  private final void sendErrorResponse(String messageId, String errorMessage, int errorCode) {
    messagingService.reply(InstallationResponse.class, messageId,
        Error.newBuilder().setCode(errorCode).setMessage(errorMessage).build());
  }

  private final void sendInstallResponse(String messageId, List<Tool> installedTools) {
    messagingService
        .reply(messageId, InstallationResponse.newBuilder().addAllTool(installedTools).build());
  }
}
