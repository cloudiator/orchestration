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

package io.github.cloudiator.iaas.vm.messaging;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.Schedulable;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import io.github.cloudiator.iaas.vm.config.VMAgentConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.inject.Named;

@Singleton
public class PerCloudExecutionService implements ExecutionService {

  private final Map<String, ExecutionService> executionServices;
  private final int workersPerCloud;

  @Inject
  public PerCloudExecutionService(
      @Named(VMAgentConstants.VM_PARALLEL_STARTS_PER_CLOUD) int workersPerCloud) {
    this.workersPerCloud = workersPerCloud;
    this.executionServices = new HashMap<>();
  }

  @Nonnull
  private synchronized ExecutionService getOrCreateExecutionService(String cloudId) {

    final LoggingScheduledThreadPoolExecutor loggingScheduledThreadPoolExecutor = new LoggingScheduledThreadPoolExecutor(
        workersPerCloud);
    MoreExecutors.addDelayedShutdownHook(loggingScheduledThreadPoolExecutor, 1, TimeUnit.MINUTES);
    return Objects.requireNonNull(
        executionServices.putIfAbsent(cloudId, new ScheduledThreadPoolExecutorExecutionService(
            loggingScheduledThreadPoolExecutor)));
  }

  @Override
  public ScheduledFuture<?> schedule(Schedulable schedulable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void execute(Runnable runnable) {
    CloudRunnable cloudRunnable = (CloudRunnable) runnable;
    getOrCreateExecutionService(cloudRunnable.cloudId()).execute(runnable);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void shutdown() {
    for (ExecutionService executionService : executionServices.values()) {
      executionService.shutdown();
    }
  }

  @Override
  public void delayShutdownHook(long terminationTimeout, TimeUnit timeUnit) {
    //intentionally left empty, each created sub execution service will have
    //a shutdown hook registered.
  }
}
