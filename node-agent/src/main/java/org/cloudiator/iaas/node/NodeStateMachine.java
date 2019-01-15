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
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.util.function.ThrowingFunction;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.TransitionBuilder;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeState;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.persistance.CloudDomainRepository;
import io.github.cloudiator.persistance.NodeDomainRepository;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messaging.services.CloudService;
import org.cloudiator.messaging.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeStateMachine implements StateMachine<Node> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeStateMachine.class);
  private final StateMachine<Node> stateMachine;
  private final CloudDomainRepository cloudDomainRepository;
  private final CloudRegistry cloudRegistry;
  private final CloudService cloudService;
  private final NodeService nodeService;
  private final NodeDomainRepository nodeDomainRepository;
  private final NodeDeletionStrategy nodeDeletionStrategy;

  @Inject
  public NodeStateMachine(
      CloudDomainRepository cloudDomainRepository,
      CloudRegistry cloudRegistry, CloudService cloudService,
      NodeService nodeService,
      NodeDomainRepository nodeDomainRepository,
      NodeDeletionStrategy nodeDeletionStrategy) {
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudRegistry = cloudRegistry;
    this.cloudService = cloudService;
    this.nodeService = nodeService;

    //noinspection unchecked
    stateMachine = StateMachineBuilder.<Node>builder().errorState(NodeState.ERROR)
        .addTransition(
            TransitionBuilder.<Node>newBuilder().from(NodeState.NEW).to(NodeState.OK)
                .action(newToOk())
                .build())
        .addTransition(
            TransitionBuilder.<Node>newBuilder().from(NodeState.OK).to(NodeState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            TransitionBuilder.<Node>newBuilder().from(NodeState.ERROR)
                .to(NodeState.DELETED)
                .action(delete())
                .build())
        .addTransition(
            TransitionBuilder.<Node>newBuilder().from(NodeState.OK).to(NodeState.ERROR)
                .action(error())
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
  void save(Node node) {
    nodeDomainRepository.save(node);
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional
  void delete(Node node) {
    nodeDomainRepository.delete(node.id());
  }


  private ThrowingFunction<Node, Node> newToOk() {

    //todo implement
    throw new UnsupportedOperationException();
  }

  private ThrowingFunction<Node, Node> delete() {

    return node -> {

      nodeDeletionStrategy.deleteNode(node);
      delete(node);

      return NodeBuilder.of(node).state(NodeState.DELETED).build();
    };
  }

  private ThrowingFunction<Node, Node> error() {

    //todo implement
    throw new UnsupportedOperationException();

  }

  @Override
  public Node apply(Node object, State to) throws ExecutionException {
    return stateMachine.apply(object, to);
  }
}
