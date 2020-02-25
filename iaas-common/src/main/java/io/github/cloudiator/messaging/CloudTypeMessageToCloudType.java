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

import de.uniulm.omi.cloudiator.sword.domain.CloudType;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;

/**
 * Created by daniel on 31.05.17.
 */
public class CloudTypeMessageToCloudType implements
    TwoWayConverter<IaasEntities.CloudType, CloudType> {

  public static final CloudTypeMessageToCloudType INSTANCE = new CloudTypeMessageToCloudType();

  private CloudTypeMessageToCloudType() {
  }

  @Override
  public IaasEntities.CloudType applyBack(CloudType cloudType) {
    switch (cloudType) {
      case PUBLIC:
        return IaasEntities.CloudType.PUBLIC_CLOUD;
      case PRIVATE:
        return IaasEntities.CloudType.PRIVATE_CLOUD;
      case SIMULATION:
        return IaasEntities.CloudType.SIMULATION_CLOUD;
      default:
        throw new AssertionError(String.format("Unrecognized cloudType %s.", cloudType));
    }
  }

  @Override
  public CloudType apply(IaasEntities.CloudType cloudType) {
    switch (cloudType) {
      case PRIVATE_CLOUD:
        return CloudType.PRIVATE;
      case PUBLIC_CLOUD:
        return CloudType.PUBLIC;
      case SIMULATION_CLOUD:
        return CloudType.SIMULATION;
      case UNRECOGNIZED:
      default:
        throw new AssertionError(String.format("Unrecognized cloudType %s", cloudType));
    }
  }
}
