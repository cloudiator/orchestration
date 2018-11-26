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

package io.github.cloudiator.orchestration.installer.remote;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.domain.Node;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 02.09.15.
 */
public class CompositeRemoteConnectionStrategy implements RemoteConnectionStrategy {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CompositeRemoteConnectionStrategy.class);

  private final Set<RemoteConnectionStrategy> strategySet;

  public CompositeRemoteConnectionStrategy(Set<RemoteConnectionStrategy> strategySet) {
    if (strategySet.isEmpty()) {
      LOGGER.warn(String.format(
          "%s is initializing with an empty strategy set. This is likely to cause errors.",
          this));
    }
    LOGGER.debug(String
        .format("%s is loading available strategy set. Contains %s strategies.", this,
            strategySet.size()));

    strategySet.forEach(remoteConnectionStrategy -> LOGGER
        .debug(String.format("%s is loading strategy %s", this, remoteConnectionStrategy)));

    // wrap in immutable sorted set to ensure comparability.
    this.strategySet = ImmutableSet.copyOf(Sets.newTreeSet(strategySet));
  }

  @Override
  public RemoteConnection connect(Node node)
      throws RemoteException {

    Exception lastException = null;
    for (RemoteConnectionStrategy remoteConnectionStrategy : strategySet) {
      try {
        LOGGER.info(String
            .format("%s is using strategy %s to connect to node %s", this,
                remoteConnectionStrategy, node));
        return remoteConnectionStrategy.connect(node);
      } catch (Exception e) {
        LOGGER.info(String
            .format("%s failed connecting to node %s using strategy %s", this,
                node, remoteConnectionStrategy), e);
        lastException = e;
      }
    }

    throw new RemoteException(
        "Tried all available remote connection strategies, but still could not connect to node.",
        lastException);
  }

  @Override
  public int getPriority() {
    if (!strategySet.isEmpty()) {
      return strategySet.stream().findFirst().get().getPriority();
    }
    return Priority.MEDIUM;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("strategies", strategySet).toString();
  }
}
