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

package io.github.cloudiator.iaas.discovery;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.iaas.discovery.config.DiscoveryModule;
import io.github.cloudiator.iaas.discovery.messaging.CloudAddedSubscriber;
import io.github.cloudiator.iaas.discovery.messaging.CloudQuerySubscriber;
import io.github.cloudiator.iaas.discovery.messaging.DeleteCloudSubscriber;
import io.github.cloudiator.iaas.discovery.messaging.DiscoveryStatusSubscriber;
import io.github.cloudiator.iaas.discovery.messaging.HardwareQuerySubscriber;
import io.github.cloudiator.iaas.discovery.messaging.ImageQuerySubscriber;
import io.github.cloudiator.iaas.discovery.messaging.LocationQuerySubscriber;
import io.github.cloudiator.persistance.JpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryAgent {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryAgent.class);

  private static Injector injector = Guice
      .createInjector(new DiscoveryModule(), new MessageServiceModule(),
          new JpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())),
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())));

  public static void main(String[] args) {

    LOGGER.info("Using configuration: " + Configuration.conf());

    final CloudAddedSubscriber instance = injector.getInstance(CloudAddedSubscriber.class);
    instance.run();

    ImageQuerySubscriber imageQuerySubscriber = injector.getInstance(ImageQuerySubscriber.class);
    imageQuerySubscriber.run();

    HardwareQuerySubscriber hardwareQuerySubscriber = injector
        .getInstance(HardwareQuerySubscriber.class);
    hardwareQuerySubscriber.run();

    LocationQuerySubscriber locationQuerySubscriber = injector
        .getInstance(LocationQuerySubscriber.class);
    locationQuerySubscriber.run();

    injector.getInstance(DeleteCloudSubscriber.class).run();

    injector.getInstance(DiscoveryStatusSubscriber.class).run();

    final CloudQuerySubscriber cloudQuerySubscriber = injector
        .getInstance(CloudQuerySubscriber.class);
    cloudQuerySubscriber.run();
  }

}
