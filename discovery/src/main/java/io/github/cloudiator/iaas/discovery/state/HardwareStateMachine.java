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

package io.github.cloudiator.iaas.discovery.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorAwareStateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorTransition.ErrorTransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.domain.DiscoveredHardware;
import io.github.cloudiator.domain.DiscoveryItemState;
import io.github.cloudiator.messaging.DiscoveryItemStateConverter;
import io.github.cloudiator.messaging.HardwareMessageToHardwareConverter;
import io.github.cloudiator.persistance.HardwareDomainRepository;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Discovery.DiscoveryEvent;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HardwareStateMachine implements ErrorAwareStateMachine<DiscoveredHardware> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HardwareStateMachine.class);
  private final ErrorAwareStateMachine<DiscoveredHardware> stateMachine;
  private final HardwareDomainRepository hardwareDomainRepository;

  @Inject
  public HardwareStateMachine(
      HardwareDomainRepository hardwareDomainRepository,
      CloudService cloudService) {
    this.hardwareDomainRepository = hardwareDomainRepository;

    //noinspection unchecked
    stateMachine = StateMachineBuilder.<DiscoveredHardware>builder()
        .addTransition(
            Transitions.<DiscoveredHardware>transitionBuilder().from(DiscoveryItemState.NEW)
                .to(DiscoveryItemState.OK).action(newToOk()).build())
        .errorTransition(
            Transitions.<DiscoveredHardware>errorTransitionBuilder()
                .errorState(DiscoveryItemState.DISABLED)
                .action(toDisabled()).build())
        .addHook(new StateMachineHook<DiscoveredHardware>() {
          @Override
          public void pre(DiscoveredHardware object, State to) {
            //intentionally left empty
          }

          @Override
          public void post(State from, DiscoveredHardware object) {
            cloudService.announceEvent(DiscoveryEvent.newBuilder().setFrom(
                DiscoveryItemStateConverter.INSTANCE.applyBack((DiscoveryItemState) from))
                .setTo(DiscoveryItemStateConverter.INSTANCE.applyBack(object.state()))
                .setHardwareFlavor(HardwareMessageToHardwareConverter.INSTANCE.applyBack(object))
                .setUserId(object.userId())
                .build());
          }
        })
        .build();
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  DiscoveredHardware save(DiscoveredHardware discoveredHardware) {
    hardwareDomainRepository.save(discoveredHardware);
    return discoveredHardware;
  }

  private TransitionAction<DiscoveredHardware> newToOk() {

    return (o, arguments) -> {
      o.setState(DiscoveryItemState.OK);
      save(o);
      return o;
    };
  }

  private ErrorTransitionAction<DiscoveredHardware> toDisabled() {
    return (o, arguments, t) -> {
      o.setState(DiscoveryItemState.DISABLED);
      save(o);
      return o;
    };
  }

  @Override
  public DiscoveredHardware apply(DiscoveredHardware object, State to, Object[] arguments) {
    return stateMachine.apply(object, to, arguments);
  }

  @Override
  public DiscoveredHardware fail(DiscoveredHardware object, Object[] arguments, Throwable t) {
    return stateMachine.fail(object, arguments, t);
  }
}
