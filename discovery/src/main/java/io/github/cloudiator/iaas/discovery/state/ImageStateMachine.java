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
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.domain.DiscoveredImage;
import io.github.cloudiator.domain.DiscoveryItemState;
import io.github.cloudiator.messaging.DiscoveryItemStateConverter;
import io.github.cloudiator.messaging.ImageMessageToImageConverter;
import io.github.cloudiator.persistance.ImageDomainRepository;
import org.cloudiator.messages.Discovery.DiscoveryEvent;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ImageStateMachine implements
    ErrorAwareStateMachine<DiscoveredImage, DiscoveryItemState> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageStateMachine.class);
  private final ErrorAwareStateMachine<DiscoveredImage, DiscoveryItemState> stateMachine;
  private final ImageDomainRepository imageDomainRepository;

  @Inject
  public ImageStateMachine(
      ImageDomainRepository imageDomainRepository,
      CloudService cloudService) {
    this.imageDomainRepository = imageDomainRepository;

    //noinspection unchecked
    stateMachine = StateMachineBuilder.<DiscoveredImage, DiscoveryItemState>builder()
        .addTransition(
            Transitions.<DiscoveredImage, DiscoveryItemState>transitionBuilder()
                .from(DiscoveryItemState.NEW)
                .to(DiscoveryItemState.OK).action(newToOk()).build())
        .errorTransition(
            Transitions.<DiscoveredImage, DiscoveryItemState>errorTransitionBuilder()
                .errorState(DiscoveryItemState.DISABLED)
                .action(toDisabled()).build())
        .addHook(new StateMachineHook<DiscoveredImage, DiscoveryItemState>() {
          @Override
          public void pre(DiscoveredImage object, DiscoveryItemState to) {
            //intentionally left empty
          }

          @Override
          public void post(DiscoveryItemState from, DiscoveredImage object) {
            cloudService.announceEvent(DiscoveryEvent.newBuilder().setFrom(
                DiscoveryItemStateConverter.INSTANCE.applyBack(from))
                .setTo(DiscoveryItemStateConverter.INSTANCE.applyBack(object.state()))
                .setImage(ImageMessageToImageConverter.INSTANCE.applyBack(object))
                .setUserId(object.userId())
                .build());
          }
        })
        .build();
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  DiscoveredImage save(DiscoveredImage DiscoveredImage) {
    imageDomainRepository.save(DiscoveredImage);
    return DiscoveredImage;
  }

  private TransitionAction<DiscoveredImage> newToOk() {

    return (o, arguments) -> {
      o.setState(DiscoveryItemState.OK);
      save(o);
      return o;
    };
  }

  private ErrorTransitionAction<DiscoveredImage> toDisabled() {
    return (o, arguments, t) -> {
      o.setState(DiscoveryItemState.DISABLED);
      save(o);
      return o;
    };
  }

  @Override
  public DiscoveredImage apply(DiscoveredImage object, DiscoveryItemState to, Object[] arguments) {
    return stateMachine.apply(object, to, arguments);
  }

  @Override
  public DiscoveredImage fail(DiscoveredImage object, Object[] arguments, Throwable t) {
    return stateMachine.fail(object, arguments, t);
  }
}
