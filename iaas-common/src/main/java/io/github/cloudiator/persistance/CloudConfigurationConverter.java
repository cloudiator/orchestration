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

package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.sword.domain.ConfigurationBuilder;
import de.uniulm.omi.cloudiator.sword.domain.Properties;
import de.uniulm.omi.cloudiator.sword.domain.PropertiesBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class CloudConfigurationConverter implements
    OneWayConverter<CloudConfigurationModel, Configuration> {

  @Nullable
  @Override
  public Configuration apply(@Nullable CloudConfigurationModel cloudConfigurationModel) {

    if (cloudConfigurationModel == null) {
      return null;
    }

    final PropertiesBuilder propertiesBuilder = PropertiesBuilder.newBuilder();
    cloudConfigurationModel.getProperties().forEach(
        propertyModel -> propertiesBuilder
            .putProperty(propertyModel.getKey(), propertyModel.getValue()));
    Properties properties = propertiesBuilder.build();

    return ConfigurationBuilder.newBuilder()
        .nodeGroup(cloudConfigurationModel.getNodeGroup()).properties(properties)
        .build();
  }
}
