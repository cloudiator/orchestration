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

package io.github.cloudiator.iaas.vm.state;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorAwareStateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorTransition;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.domain.ExtendedVirtualMachine;
import io.github.cloudiator.domain.LocalVirtualMachineState;
import io.github.cloudiator.messaging.VirtualMachineMessageToVirtualMachine;
import io.github.cloudiator.messaging.VirtualMachineMessageToVirtualMachine.VirtualMachineStateConverter;
import io.github.cloudiator.persistance.VirtualMachineDomainRepository;
import org.cloudiator.messages.Vm.VirtualMachineEvent;
import org.cloudiator.messaging.services.VirtualMachineService;

@Singleton
public class VirtualMachineStateMachine implements ErrorAwareStateMachine<ExtendedVirtualMachine> {

  private final ErrorAwareStateMachine<ExtendedVirtualMachine> delegate;
  private final VirtualMachineDomainRepository virtualMachineDomainRepository;
  private final VirtualMachineService virtualMachineService;

  @Inject public VirtualMachineStateMachine(
      VirtualMachineDomainRepository virtualMachineDomainRepository,
      VirtualMachineService virtualMachineService) {
    this.virtualMachineDomainRepository = virtualMachineDomainRepository;
    this.virtualMachineService = virtualMachineService;
    //noinspection unchecked
    delegate = StateMachineBuilder.<ExtendedVirtualMachine>builder().errorTransition(error())
        .addHook(
            new StateMachineHook<ExtendedVirtualMachine>() {
              @Override
              public void pre(ExtendedVirtualMachine extendedVirtualMachine, State to) {
                //intentionally left empty
              }

              @Override
              public void post(State from, ExtendedVirtualMachine extendedVirtualMachine) {
                VirtualMachineStateMachine.this.virtualMachineService
                    .announceEvent(VirtualMachineEvent.newBuilder().setFrom(
                        VirtualMachineStateConverter.INSTANCE
                            .apply((LocalVirtualMachineState) from))
                        .setTo(VirtualMachineStateConverter.INSTANCE
                            .apply(extendedVirtualMachine.state()))
                        .setVm(VirtualMachineMessageToVirtualMachine.INSTANCE
                            .applyBack(extendedVirtualMachine))
                        .setUserId(extendedVirtualMachine.getUserId()).build());
              }
            })
        .build();
  }

  @Transactional
  synchronized ExtendedVirtualMachine save(ExtendedVirtualMachine virtualMachine) {
    virtualMachineDomainRepository.save(virtualMachine);
    return virtualMachine;
  }

  private ErrorTransition<ExtendedVirtualMachine> error() {

    return Transitions.<ExtendedVirtualMachine>errorTransitionBuilder()
        .action((o, arguments, throwable) -> {

          o.setState(LocalVirtualMachineState.ERROR);
          save(o);

          return o;

        })
        .errorState(LocalVirtualMachineState.ERROR).build();
  }

  @Override
  public ExtendedVirtualMachine fail(ExtendedVirtualMachine object, Object[] arguments,
      Throwable t) {
    return delegate.fail(object, arguments, t);
  }

  @Override
  public ExtendedVirtualMachine apply(ExtendedVirtualMachine object, State to, Object[] arguments) {
    return delegate.apply(object, to, arguments);
  }
}
