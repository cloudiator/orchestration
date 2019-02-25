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
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorAwareStateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorTransition;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.ExtendedCloudBuilder;
import io.github.cloudiator.domain.ExtendedCloudImpl;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter.CloudStateConverter;
import io.github.cloudiator.persistance.CloudDomainRepository;
import org.cloudiator.messages.Cloud.CloudEvent;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CloudStateMachine implements ErrorAwareStateMachine<ExtendedCloud> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloudStateMachine.class);
  private final ErrorAwareStateMachine<ExtendedCloud> stateMachine;
  private final CloudDomainRepository cloudDomainRepository;
  private final CloudRegistry cloudRegistry;
  private final CloudService cloudService;

  @Inject
  public CloudStateMachine(
      CloudDomainRepository cloudDomainRepository,
      CloudRegistry cloudRegistry, CloudService cloudService) {
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudRegistry = cloudRegistry;
    this.cloudService = cloudService;

    //noinspection unchecked
    stateMachine = StateMachineBuilder.<ExtendedCloud>builder().errorTransition(error())
        .addTransition(
            Transitions.<ExtendedCloud>transitionBuilder().from(CloudState.NEW).to(CloudState.OK)
                .action(newToOk())
                .build())
        .addTransition(
            Transitions.<ExtendedCloud>transitionBuilder().from(CloudState.OK)
                .to(CloudState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            Transitions.<ExtendedCloud>transitionBuilder().from(CloudState.ERROR)
                .to(CloudState.DELETED)
                .action(delete())
                .build())
        .addHook(new StateMachineHook<ExtendedCloud>() {
          @Override
          public void pre(ExtendedCloud cloud, State to) {
            //intentionally left empty
          }

          @Override
          public void post(State from, ExtendedCloud cloud) {
            final CloudEvent cloudEvent = CloudEvent.newBuilder().setUserId(cloud.userId())
                .setCloud(
                    CloudMessageToCloudConverter.INSTANCE.applyBack(cloud))
                .setFrom(CloudStateConverter.INSTANCE.applyBack(
                    (CloudState) from)).setTo(CloudStateConverter.INSTANCE.applyBack(cloud.state()))
                .build();
            LOGGER.debug(String
                .format(
                    "Executing post hook to announce cloud changed event for cloud %s. Previous state was %s, new state is %s.",
                    cloud, from, cloud.state()));
            cloudService.announceEvent(cloudEvent);
          }
        })
        .build();

  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void save(ExtendedCloud extendedCloud) {
    cloudDomainRepository.save(extendedCloud);
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void delete(ExtendedCloud extendedCloud) {
    cloudDomainRepository.delete(extendedCloud.id(), extendedCloud.userId());
  }


  private TransitionAction<ExtendedCloud> newToOk() {

    return (extendedCloud, arguments) -> {

      final ExtendedCloudImpl ok = ExtendedCloudBuilder.of(extendedCloud).state(CloudState.OK)
          .build();
      CloudStateMachine.this.save(ok);
      cloudRegistry.register(ok);

      return ok;
    };

  }

  private TransitionAction<ExtendedCloud> delete() {

    return (extendedCloud, arguments) -> {

      final ExtendedCloudImpl deleted = ExtendedCloudBuilder.of(extendedCloud)
          .state(CloudState.DELETED)
          .build();

      delete(extendedCloud);

      cloudRegistry.unregister(extendedCloud.id());

      return deleted;
    };

  }

  private ErrorTransition<ExtendedCloud> error() {

    return Transitions.<ExtendedCloud>errorTransitionBuilder()
        .action((extendedCloud, arguments, t) -> {

          final ExtendedCloudBuilder builder = ExtendedCloudBuilder.of(extendedCloud)
              .state(CloudState.ERROR);
          if (t != null) {
            builder.diagnostic(t.getMessage());
          }

          final ExtendedCloudImpl cloud = builder.build();

          save(cloud);
          cloudRegistry.unregister(cloud.id());

          return cloud;
        }).errorState(CloudState.ERROR).build();

  }

  @Override
  public ExtendedCloud apply(ExtendedCloud object, State to, Object[] arguments) {
    return stateMachine.apply(object, to, arguments);
  }

  @Override
  public ExtendedCloud fail(ExtendedCloud object, Object[] arguments, Throwable t) {
    return stateMachine.fail(object, arguments, t);
  }
}
