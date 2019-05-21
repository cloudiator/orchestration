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

package io.github.cloudiator.iaas.byon;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.iaas.byon.config.ByonModule;
import io.github.cloudiator.persistance.JpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ByonAgent {

  private static final Injector INJECTOR = Guice
      .createInjector(new ByonModule(), new JpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())), new MessageServiceModule(),
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())));
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonAgent.class);

  /**
   * the main method.
   *
   * @param args args
   */
  public static void main(String[] args) {
    LOGGER.info("Using configuration: " + Configuration.conf());

    LOGGER.debug("Starting listeners.");
    //rest-server
    LOGGER.debug("Starting " + AddByonNodeSubscriber.class);
    INJECTOR.getInstance(AddByonNodeSubscriber.class).run();
    LOGGER.debug("Starting " + RemoveByonNodeSubscriber.class);
    INJECTOR.getInstance(RemoveByonNodeSubscriber.class).run();
    //matchmaking-agent
    LOGGER.debug("Starting " + ByonNodeQuerySubscriber.class);
    INJECTOR.getInstance(ByonNodeQuerySubscriber.class).run();
    //node-agent
    LOGGER.debug("Starting " + ByonNodeAllocateRequestListener.class);
    INJECTOR.getInstance(ByonNodeAllocateRequestListener.class).run();
    LOGGER.debug("Starting " + ByonNodeDeleteRequestListener.class);
    INJECTOR.getInstance(ByonNodeDeleteRequestListener.class).run();

    LOGGER.debug("Finished starting listeners.");
  }
}
