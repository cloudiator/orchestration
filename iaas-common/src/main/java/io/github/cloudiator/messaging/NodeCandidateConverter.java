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

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.*;
import io.github.cloudiator.domain.Runtime;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class NodeCandidateConverter implements
    OneWayConverter<MatchmakingEntities.NodeCandidate, NodeCandidate> {

  public static final NodeCandidateConverter INSTANCE = new NodeCandidateConverter();

  private static final ImageMessageToImageConverter IMAGE_CONVERTER = ImageMessageToImageConverter.INSTANCE;
  private static final HardwareMessageToHardwareConverter HARDWARE_CONVERTER = HardwareMessageToHardwareConverter.INSTANCE;
  private static final LocationMessageToLocationConverter LOCATION_CONVERTER = LocationMessageToLocationConverter.INSTANCE;
  private static final CloudMessageToCloudConverter CLOUD_CONVERTER = CloudMessageToCloudConverter.INSTANCE;
  private static final NodeCandidateTypeConverter TYPE_CONVERTER = NodeCandidateTypeConverter.INSTANCE;
  private static final EnvironmentConverter ENVIRONMENT_CONVERTER = EnvironmentConverter.INSTANCE;

  private NodeCandidateConverter() {
  }

  @Override
  public NodeCandidate apply(MatchmakingEntities.NodeCandidate nodeCandidate) {
    switch (nodeCandidate.getType()) {
      case NC_IAAS:
        return applyIaas(nodeCandidate);
      case NC_FAAS:
        return applyFaas(nodeCandidate);
      case NC_PAAS:
      case NC_BYON:
      case UNRECOGNIZED:
      default:
        throw new IllegalStateException("Unsupported node candidate type: " + nodeCandidate.getType());
    }
  }

  private NodeCandidate applyIaas(MatchmakingEntities.NodeCandidate nodeCandidate) {
    return NodeCandidateBuilder.create()
        .type(TYPE_CONVERTER.apply(nodeCandidate.getType()))
        .cloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .image(IMAGE_CONVERTER.apply(nodeCandidate.getImage()))
        .location(LOCATION_CONVERTER.apply(nodeCandidate.getLocation()))
        .hardware(HARDWARE_CONVERTER.apply(nodeCandidate.getHardwareFlavor()))
        .price(nodeCandidate.getPrice())
        .build();
  }

  private NodeCandidate applyFaas(MatchmakingEntities.NodeCandidate nodeCandidate) {
    return NodeCandidateBuilder.create()
        .type(TYPE_CONVERTER.apply(nodeCandidate.getType()))
        .cloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .location(LOCATION_CONVERTER.apply(nodeCandidate.getLocation()))
        .hardware(HARDWARE_CONVERTER.apply(nodeCandidate.getHardwareFlavor()))
        .pricePerInvocation(nodeCandidate.getPricePerInvocation())
        .memoryPrice(nodeCandidate.getMemoryPrice())
        .environment(ENVIRONMENT_CONVERTER.apply(nodeCandidate.getEnvironment()))
        .build();
  }
}
