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

package org.cloudiator.iaas.node.messaging;

import static org.cloudiator.iaas.node.config.Constants.NODE_EXECUTION_SERVICE_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import javax.inject.Named;
import org.cloudiator.iaas.node.messaging.NodeWorker.NodeRequest;
import org.cloudiator.messages.Node.NodeRequestMessage;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeRequestListener.class);
  private final MessageInterface messageInterface;
  private final ExecutorService nodeExecutorService;
  private final NodeRequestWorkerFactory nodeRequestWorkerFactory;


  @Inject
  public NodeRequestListener(MessageInterface messageInterface,
      @Named(NODE_EXECUTION_SERVICE_NAME)
          ExecutorService nodeExecutorService,
      NodeRequestWorkerFactory nodeRequestWorkerFactory) {
    this.messageInterface = messageInterface;
    this.nodeExecutorService = nodeExecutorService;
    this.nodeRequestWorkerFactory = nodeRequestWorkerFactory;
  }


  @Override
  public void run() {
    messageInterface.subscribe(NodeRequestMessage.class, NodeRequestMessage.parser(),
        (id, content) -> {
          LOGGER.info(String.format("Receiving new node request %s. ", content));

          nodeExecutorService.submit(nodeRequestWorkerFactory.create(NodeRequest.of(id, content)));
        });
  }
}
