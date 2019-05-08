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
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;
import de.uniulm.omi.cloudiator.util.stateMachine.Transitions;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.NodeDomainRepository;
import io.github.cloudiator.persistance.TransactionRetryer;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeStateMachine implements ErrorAwareStateMachine<Node, NodeState> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeStateMachine.class);
  private final ErrorAwareStateMachine<Node, NodeState> stateMachine;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeDeletionStrategy nodeDeletionStrategy;
  private final NodeSchedulingStrategy nodeSchedulingStrategy;

  @Inject
  public NodeStateMachine(
      NodeService nodeService,
      NodeDomainRepository nodeDomainRepository,
      NodeDeletionStrategy nodeDeletionStrategy,
      NodeSchedulingStrategy nodeSchedulingStrategy) {

    //noinspection unchecked
    stateMachine = StateMachineBuilder.<Node, NodeState>builder().errorTransition(error())
        .addTransition(
            Transitions.<Node, NodeState>transitionBuilder().from(NodeState.PENDING)
                .to(NodeState.RUNNING)
                .action(pendingToRunning())
                .build())
        .addTransition(
            Transitions.<Node, NodeState>transitionBuilder().from(NodeState.RUNNING)
                .to(NodeState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            Transitions.<Node, NodeState>transitionBuilder().from(NodeState.ERROR)
                .to(NodeState.DELETED)
                .action(delete())
                .build())
        .addHook(new StateMachineHook<Node, NodeState>() {
          @Override
          public void pre(Node node, NodeState to) {
            //intentionally left empty
          }

          @Override
          public void post(NodeState from, Node node) {

            final NodeEvent nodeEvent = NodeEvent.newBuilder()
                .setNode(NodeToNodeMessageConverter.INSTANCE.apply(node))
                .setFrom(NodeToNodeMessageConverter.NODE_STATE_CONVERTER.apply(
                    from))
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
    this.nodeSchedulingStrategy = nodeSchedulingStrategy;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  synchronized Node save(Node node) {
    nodeDomainRepository.save(node);
    return node;
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  synchronized Node delete(Node node) {
    nodeDomainRepository.delete(node.id());
    return node;
  }


  private TransitionAction<Node> pendingToRunning() {

    return (o, arguments) -> {

      if (!nodeSchedulingStrategy.canSchedule(o)) {
        throw new ExecutionException(new IllegalStateException(
            String.format("NodeScheduleStrategy %s does not support scheduling of node %s.",
                nodeSchedulingStrategy, o)));
      }
      try {
        final Node schedule = nodeSchedulingStrategy.schedule(o);
        TransactionRetryer.retry(() -> save(schedule));
        return schedule;
      } catch (NodeSchedulingException e) {
        throw new ExecutionException(e);
      }
    };
  }

  private TransitionAction<Node> delete() {

    return (node, arguments) -> {

      nodeDeletionStrategy.deleteNode(node);
      TransactionRetryer.retry(() -> delete(node));

      return NodeBuilder.of(node).state(NodeState.DELETED).build();
    };
  }

  private ErrorTransition<Node, NodeState> error() {

    return Transitions.<Node, NodeState>errorTransitionBuilder()
        .action((o, arguments, throwable) -> {

          final NodeBuilder builder = NodeBuilder.of(o).state(NodeState.ERROR);
          if (throwable != null) {
            builder.diagnostic(throwable.getMessage());
          }
          return TransactionRetryer.retry(() -> save(builder.build()));
        })
        .errorState(NodeState.ERROR).build();
  }

  @Override
  public Node apply(Node object, NodeState to, Object[] arguments) {
    return stateMachine.apply(object, to, arguments);
  }

  @Override
  public Node fail(Node object, Object[] arguments, Throwable t) {
    return stateMachine.fail(object, arguments, t);
  }
}
