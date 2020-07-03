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

import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.orchestration.installer.tools.installer.Installers;
import io.github.cloudiator.orchestration.installer.tools.installer.api.InstallApi;
import io.github.cloudiator.persistance.CloudDomainRepository;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Installation.InstallationResponse;
import org.cloudiator.messages.InstallationEntities.Tool;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Daniel Seybold on 12.09.2017.
 */
public class InstallEventSubscriber implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(InstallEventSubscriber.class);
  private static final int SERVER_ERROR = 500;
  private final MessageInterface messagingService;
  private static final NodeToNodeMessageConverter NODE_MESSAGE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private static final Semaphore SEMAPHORE = new Semaphore(
      Configuration.conf().getInt("installer.parallelism"), true);
  private final CloudDomainRepository cloudDomainRepository;


  @Inject
  public InstallEventSubscriber(MessageInterface messageInterface, CloudDomainRepository cloudDomainRepository) {
    this.messagingService = messageInterface;
    this.cloudDomainRepository = cloudDomainRepository;
  }

  @Override
  public void run() {

    final Subscription subscribe = messagingService.subscribe(InstallationRequest.class,
        InstallationRequest.parser(), (requestId, InstallationRequest) -> {

          try {
            SEMAPHORE.acquire();
            List<Tool> installedTools = handleRequest(requestId,
                InstallationRequest);
            sendInstallResponse(requestId, installedTools);
          } catch (Exception ex) {
            LOGGER.error("exception occurred.", ex);
            sendErrorResponse(requestId,
                "exception occurred: " + ex.getMessage(), SERVER_ERROR);
          } finally {
            SEMAPHORE.release();
          }
        });
  }

  private final List<Tool> handleRequest(String requestId, InstallationRequest installationRequest)
      throws RemoteException {
    //TODO: implement queue + worker as done for NodeEvent to enable mutlipe installations in parallel

    LOGGER.debug("Received installRequest with requestId: " + requestId);

    Node node = NODE_MESSAGE_CONVERTER
        .applyBack(installationRequest.getInstallation().getNode());

    LOGGER.debug("Install tools on node with user: {}", node.loginCredential().isPresent() ? node.loginCredential().get().username() : "empty username");

    List<Tool> installedTools;

    try (RemoteConnection remoteConnection = node.connect()) {
      installedTools = installTools(installationRequest.getInstallation().getToolList(),
          remoteConnection, node,
          installationRequest.getUserId());

      // disable firewall for Oktawave
      final ExtendedCloud cloud = cloudDomainRepository
              .findById(node.nodeProperties().providerId());
      if (cloud != null && "oktawave".equals(cloud.api().providerName())) {
        LOGGER.debug("Found cloud: %s for node with id=%s", cloud.api().providerName(), node.id());
        disableFirewall(remoteConnection, node, installationRequest.getUserId());
      }

    }

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
      } else if (tool.equals(Tool.DLMS_AGENT)) {
        installApi.installDlmsAgent();
      } else if (tool.equals(Tool.ALLUXIO_CLIENT)) {
        LOGGER.warn("ALLUXIO_CLIENT installation will be discarded");
      } else if (tool.equals(Tool.SPARK_WORKER)) {
        installApi.installSparkWorker();
        installedTools.add(tool);
      } else if (tool.equals(Tool.HDFS_DATA)) {
        installApi.installHdfsDataNode();
        installedTools.add(tool);
      } else if (tool.equals(Tool.EMS_CLIENT)) {
        installApi.installEMS();
        installedTools.add(tool);
      } else {
        throw new IllegalStateException("Unsupported toolName: " + tool.name());
      }
    }

    return installedTools;

  }

  public void disableFirewall(RemoteConnection remoteConnection, Node node,
                              String userId) throws RemoteException {
    InstallApi installApi = Installers.of(remoteConnection, node, userId);
    installApi.bootstrap();
    installApi.disableFirewall();
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
