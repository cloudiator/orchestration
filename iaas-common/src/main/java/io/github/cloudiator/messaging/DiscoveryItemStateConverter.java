/*
 * Copyright (c) 2014-2019 University of Ulm
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

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.DiscoveryItemState;
import org.cloudiator.messages.entities.IaasEntities;

public class DiscoveryItemStateConverter implements
    TwoWayConverter<IaasEntities.DiscoveryItemState, DiscoveryItemState> {

  public final static DiscoveryItemStateConverter INSTANCE = new DiscoveryItemStateConverter();

  private DiscoveryItemStateConverter() {
  }

  ;

  @Override
  public IaasEntities.DiscoveryItemState applyBack(DiscoveryItemState discoveryItemState) {

    switch (discoveryItemState) {
      case OK:
        return IaasEntities.DiscoveryItemState.DISCOVERY_OK;
      case NEW:
        return IaasEntities.DiscoveryItemState.DISCOVERY_NEW;
      case UNKNOWN:
        return IaasEntities.DiscoveryItemState.DISCOVERY_UNKNOWN;
      case DISABLED:
        return IaasEntities.DiscoveryItemState.DISCOVERY_DISABLED;
      case LOCALLY_DELETED:
        return IaasEntities.DiscoveryItemState.DISCOVERY_LOCALLY_DELETED;
      case REMOTELY_DELETED:
        return IaasEntities.DiscoveryItemState.DISCOVERY_REMOTELY_DELETED;
      default:
        throw new AssertionError("Unknown state " + discoveryItemState);
    }

  }

  @Override
  public DiscoveryItemState apply(IaasEntities.DiscoveryItemState discoveryItemState) {
    switch (discoveryItemState) {
      case DISCOVERY_REMOTELY_DELETED:
        return DiscoveryItemState.REMOTELY_DELETED;
      case DISCOVERY_LOCALLY_DELETED:
        return DiscoveryItemState.LOCALLY_DELETED;
      case DISCOVERY_DISABLED:
        return DiscoveryItemState.DISABLED;
      case DISCOVERY_UNKNOWN:
        return DiscoveryItemState.UNKNOWN;
      case DISCOVERY_NEW:
        return DiscoveryItemState.NEW;
      case DISCOVERY_OK:
        return DiscoveryItemState.OK;
      case UNRECOGNIZED:
      default:
        throw new AssertionError("Unkown state " + discoveryItemState);
    }
  }
}
