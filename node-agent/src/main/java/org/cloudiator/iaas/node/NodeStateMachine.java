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

package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorAwareStateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.ErrorTransition;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.NodeDomainRepository;
import java.util.concurrent.ExecutionException;
import org.cloudiator.iaas.node.NodeCandidateIncarnationStrategy.NodeCandidateIncarnationFactory;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeStateMachine implements ErrorAwareStateMachine<Node> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeStateMachine.class);
  private final ErrorAwareStateMachine<Node> stateMachine;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeDeletionStrategy nodeDeletionStrategy;

  @Inject
  public NodeStateMachine(
      NodeService nodeService,
      NodeDomainRepository nodeDomainRepository,
      NodeDeletionStrategy nodeDeletionStrategy) {

    //noinspection unchecked
    stateMachine = StateMachineBuilder.<Node>builder().errorTransition(error())
        .addTransition(
            Transitions.<Node>transitionBuilder().from(NodeState.CREATED).to(NodeState.RUNNING)
                .action(createdToRunning())
                .build())
        .addTransition(
            Transitions.<Node>transitionBuilder().from(NodeState.RUNNING).to(NodeState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            Transitions.<Node>transitionBuilder().from(NodeState.ERROR)
                .to(NodeState.DELETED)
                .action(delete())
                .build())
        .addHook(new StateMachineHook<Node>() {
          @Override
          public void pre(Node node, State to) {
            //intentionally left empty
          }

          @Override
          public void post(State from, Node node) {

            final NodeEvent nodeEvent = NodeEvent.newBuilder()
                .setNode(NodeToNodeMessageConverter.INSTANCE.apply(node))
                .setFrom(NodeToNodeMessageConverter.NODE_STATE_CONVERTER.apply(
                    (NodeState) from))
                .setTo(NodeToNodeMessageConverter.NODE_STATE_CONVERTER.apply(node.state())).build();

            LOGGER.debug(String
                .format(
                    "Executing post hook to announce node changed event for node %s. Previous state was %s, new state is %s.",
                    node, from, node.state()));
            nodeService.announceNodeEvent(nodeEvent);
          }
        })
        .build();

    this.nodeDomainRepository = nodeDomainRepository;
    this.nodeDeletionStrategy = nodeDeletionStrategy;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  Node save(Node node) {
    nodeDomainRepository.save(node);
    return node;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void delete(Node node) {
    nodeDomainRepository.delete(node.id());
  }


  private TransitionAction<Node> createdToRunning() {

    return (o, arguments) -> {
      final Node running = NodeBuilder.of(o).state(NodeState.RUNNING).build();
      save(running);
      return running;
    };
  }

  private TransitionAction<Node> delete() {

    return (node, arguments) -> {

      nodeDeletionStrategy.deleteNode(node);
      delete(node);

      return NodeBuilder.of(node).state(NodeState.DELETED).build();
    };
  }

  private ErrorTransition<Node> error() {

    return Transitions.<Node>errorTransitionBuilder()
        .action((o, arguments, throwable) -> {

          final NodeBuilder builder = NodeBuilder.of(o).state(NodeState.ERROR);
          if (throwable != null) {
            builder.diagnostic(throwable.getMessage());
          }
          return save(builder.build());
        })
        .errorState(NodeState.ERROR).build();
  }

  @Override
  public Node apply(Node object, State to, Object[] arguments) throws ExecutionException {
    return stateMachine.apply(object, to, arguments);
  }

  @Override
  public Node fail(Node object, Object[] arguments, Throwable t) {
    return stateMachine.fail(object, arguments, t);
  }
}
