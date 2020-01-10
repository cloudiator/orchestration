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

package io.github.cloudiator.remote;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.remote.RemoteConnection;
import de.uniulm.omi.cloudiator.sword.remote.RemoteException;
import io.github.cloudiator.domain.BaseNode;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 02.09.15.
 */
public class CompositeRemoteConnectionStrategy implements RemoteConnectionStrategy {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CompositeRemoteConnectionStrategy.class);
  private static final RemoteLock REMOTE_LOCK = new RemoteLock(10);

  private static final Set<RemoteConnectionStrategy> STRATEGIES = new TreeSet<RemoteConnectionStrategy>() {{
    add(new PasswordRemoteConnectionStrategy());
    add(new KeyPairRemoteConnectionStrategy());
  }};

  public CompositeRemoteConnectionStrategy() {
    if (STRATEGIES.isEmpty()) {
      LOGGER.warn(String.format(
          "%s is initializing with an empty strategy set. This is likely to cause errors.",
          this));
    }
    LOGGER.debug(String
        .format("%s is loading available strategy set. Contains %s strategies.", this,
            STRATEGIES.size()));

    STRATEGIES.forEach(remoteConnectionStrategy -> LOGGER
        .debug(String.format("%s has strategy %s", this, remoteConnectionStrategy)));
  }

  @Override
  public RemoteConnection connect(BaseNode node)
      throws RemoteException {

    try {
      REMOTE_LOCK.aquire();
    } catch (InterruptedException e) {
      throw new RemoteException("Interrupted while waiting for remote lock");
    }

    try {
      Exception lastException = null;
      for (RemoteConnectionStrategy remoteConnectionStrategy : STRATEGIES) {
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
    } finally {
      REMOTE_LOCK.release();
    }

  }

  @Override
  public int getPriority() {
    if (!STRATEGIES.isEmpty()) {
      return STRATEGIES.stream().findFirst().get().getPriority();
    }
    return Priority.MEDIUM;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("strategies", STRATEGIES).toString();
  }
}
