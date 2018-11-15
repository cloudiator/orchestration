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
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodeCandidateBuilder;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class NodeCandidateConverter implements
    OneWayConverter<MatchmakingEntities.NodeCandidate, NodeCandidate> {

  public static final NodeCandidateConverter INSTANCE = new NodeCandidateConverter();

  private static final ImageMessageToImageConverter IMAGE_CONVERTER = ImageMessageToImageConverter.INSTANCE;
  private static final HardwareMessageToHardwareConverter HARDWARE_CONVERTER = HardwareMessageToHardwareConverter.INSTANCE;
  private static final LocationMessageToLocationConverter LOCATION_CONVERTER = LocationMessageToLocationConverter.INSTANCE;
  private static final CloudMessageToCloudConverter CLOUD_CONVERTER = CloudMessageToCloudConverter.INSTANCE;

  private NodeCandidateConverter() {
  }

  @Override
  public NodeCandidate apply(MatchmakingEntities.NodeCandidate nodeCandidate) {

    return NodeCandidateBuilder.create()
        .cloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .image(IMAGE_CONVERTER.apply(nodeCandidate.getImage()))
        .location(LOCATION_CONVERTER.apply(nodeCandidate.getLocation()))
        .hardware(HARDWARE_CONVERTER.apply(nodeCandidate.getHardwareFlavor()))
        .price(nodeCandidate.getPrice())
        .build();
  }
}
