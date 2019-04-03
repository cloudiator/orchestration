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

package org.cloudiator.iaas.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.persistance.JpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.iaas.node.config.NodeAgentContext;
import org.cloudiator.iaas.node.config.NodeModule;
import org.cloudiator.iaas.node.messaging.NodeDeleteRequestListener;
import org.cloudiator.iaas.node.messaging.NodeQueryListener;
import org.cloudiator.iaas.node.messaging.NodeRequestListener;
import org.cloudiator.iaas.node.messaging.VirtualMachineEventSubscriber;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeAgent {

  private static final Injector INJECTOR = Guice
      .createInjector(new NodeModule(new NodeAgentContext()), new JpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())), new MessageServiceModule(),
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())));
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeAgent.class);

  public static void main(String[] args) {

    LOGGER.debug("Starting listeners.");
    LOGGER.debug("Starting " + NodeRequestListener.class);
    INJECTOR.getInstance(NodeRequestListener.class).run();
    LOGGER.debug("Starting " + NodeQueryListener.class);
    INJECTOR.getInstance(NodeQueryListener.class).run();
    LOGGER.debug("Starting " + NodeDeleteRequestListener.class);
    INJECTOR.getInstance(NodeDeleteRequestListener.class).run();
    LOGGER.debug("Starting " + VirtualMachineEventSubscriber.class);
    INJECTOR.getInstance(VirtualMachineEventSubscriber.class).run();

    LOGGER.debug("Finished starting listeners.");
  }

}
