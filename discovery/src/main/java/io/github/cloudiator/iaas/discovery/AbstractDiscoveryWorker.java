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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.multicloud.exception.MultiCloudException;
import de.uniulm.omi.cloudiator.sword.service.DiscoveryService;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 25.01.17.
 */
public abstract class AbstractDiscoveryWorker<T> implements Schedulable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiscoveryWorker.class);

  private final DiscoveryQueue discoveryQueue;
  private final DiscoveryService discoveryService;
  private final DiscoveryErrorHandler discoveryErrorHandler;

  @Inject
  public AbstractDiscoveryWorker(DiscoveryQueue discoveryQueue, DiscoveryService discoveryService,
      DiscoveryErrorHandler discoveryErrorHandler) {
    checkNotNull(discoveryQueue, "discoveryQueue is null");
    this.discoveryQueue = discoveryQueue;
    checkNotNull(discoveryService, "discoveryService is null");
    this.discoveryService = discoveryService;
    checkNotNull(discoveryErrorHandler, "discoveryErrorHandler is null");
    this.discoveryErrorHandler = discoveryErrorHandler;
  }

  protected abstract Iterable<T> resources(DiscoveryService discoveryService);

  @Override
  public final long period() {
    return 60;
  }

  @Override
  public final long delay() {
    return 0;
  }

  @Override
  public final TimeUnit timeUnit() {
    return TimeUnit.SECONDS;
  }

  @Override
  public void run() {
    LOGGER.info(String.format("%s is starting new discovery run", this));
    try {
      StreamSupport.stream(resources(discoveryService).spliterator(), false).map(Discovery::new)
          .forEach(discovery -> {
            LOGGER.trace(String.format("%s found discovery %s", this, discovery));
            discoveryQueue.add(discovery);
          });
    } catch (MultiCloudException e) {
      LOGGER.error(String.format(
          "%s caught multi cloud exception %s during discovery run. Exception was caught and send to error handler %s.",
          this, e.getMessage(), discoveryErrorHandler), e);
      discoveryErrorHandler.report(e);
    } catch (Exception e) {
      LOGGER.error(String.format(
          "%s reported exception %s during discovery run. Exception was caught to allow further executions.",
          this, e.getMessage()), e);
    }
    LOGGER.info(String.format("%s finished discovery run", this));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("discoveryQueue", discoveryQueue).toString();
  }
}
