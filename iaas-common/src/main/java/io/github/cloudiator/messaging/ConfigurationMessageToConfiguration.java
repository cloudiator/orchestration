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

package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.sword.domain.ConfigurationBuilder;
import de.uniulm.omi.cloudiator.sword.domain.PropertiesBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Configuration.Builder;

/**
 * Created by daniel on 31.05.17.
 */
class ConfigurationMessageToConfiguration
    implements TwoWayConverter<IaasEntities.Configuration, Configuration> {

  public static final ConfigurationMessageToConfiguration INSTANCE = new ConfigurationMessageToConfiguration();

  private ConfigurationMessageToConfiguration() {
  }

  @Override
  public Configuration apply(IaasEntities.Configuration configuration) {
    final PropertiesBuilder propertiesBuilder = PropertiesBuilder.newBuilder();
    configuration.getPropertiesMap()
        .forEach(propertiesBuilder::putProperty);
    return ConfigurationBuilder.newBuilder().nodeGroup(configuration.getNodeGroup())
        .properties(propertiesBuilder.build()).build();
  }

  @Override
  public IaasEntities.Configuration applyBack(Configuration configuration) {
    final Builder builder = IaasEntities.Configuration.newBuilder();
    configuration.properties().getProperties().forEach(
        builder::putProperties);
    builder.setNodeGroup(configuration.nodeGroup());
    return builder.build();
  }
}
