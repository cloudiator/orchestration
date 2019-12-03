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

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import io.github.cloudiator.domain.DiscoveredHardware;
import io.github.cloudiator.domain.DiscoveryItemState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.iaas.discovery.state.HardwareStateMachine;
import io.github.cloudiator.persistance.CloudDomainRepository;
import io.github.cloudiator.persistance.HardwareDomainRepository;
import io.github.cloudiator.persistance.MissingLocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 02.06.17.
 */
public class HardwareDiscoveryListener implements DiscoveryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageDiscoveryListener.class);
  private final HardwareDomainRepository hardwareDomainRepository;
  private final CloudDomainRepository cloudDomainRepository;
  private final HardwareStateMachine hardwareStateMachine;

  @Inject
  public HardwareDiscoveryListener(
      HardwareDomainRepository hardwareDomainRepository,
      CloudDomainRepository cloudDomainRepository,
      HardwareStateMachine hardwareStateMachine) {
    this.hardwareDomainRepository = hardwareDomainRepository;
    this.cloudDomainRepository = cloudDomainRepository;
    this.hardwareStateMachine = hardwareStateMachine;
  }

  @Override
  public Class<?> interestedIn() {
    return HardwareFlavor.class;
  }

  @Override
  @Transactional
  public void handle(Object o) {

    final HardwareFlavor hardwareFlavor = (HardwareFlavor) o;

    final DiscoveredHardware byId = hardwareDomainRepository.findById(hardwareFlavor.id());

    if (byId != null) {
      LOGGER.trace(String.format("Skipping hardware %s. Already exists.", hardwareFlavor));
      return;
    }

    final ExtendedCloud cloud = cloudDomainRepository
        .findById(IdScopedByClouds.from(hardwareFlavor.id()).cloudId());

    if (cloud == null) {
      throw new IllegalStateException(
          String.format("Cloud for hardware %s is not available", hardwareFlavor));
    }

    DiscoveredHardware discoveredHardware = new DiscoveredHardware(hardwareFlavor,
        DiscoveryItemState.NEW, cloud.userId());
    try {
      hardwareDomainRepository.save(discoveredHardware);
      hardwareStateMachine.apply(discoveredHardware, DiscoveryItemState.OK, new Object[0]);
    } catch (MissingLocationException e) {
      LOGGER
          .info(String
              .format("Skipping discovery of hardware %s as assigned location seems to be missing.",
                  discoveredHardware), e);
    }

  }
}
