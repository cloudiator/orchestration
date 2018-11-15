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

package io.github.cloudiator.iaas.vm;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import de.uniulm.omi.cloudiator.util.statistics.Metric;
import de.uniulm.omi.cloudiator.util.statistics.MetricBuilder;
import de.uniulm.omi.cloudiator.util.statistics.StatisticInterface;
import io.github.cloudiator.messaging.CloudMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineStatistics {

  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualMachineStatistics.class);

  private final StatisticInterface statisticInterface;
  private final CloudMessageRepository cloudMessageRepository;

  @Inject
  public VirtualMachineStatistics(
      StatisticInterface statisticInterface,
      CloudMessageRepository cloudMessageRepository) {
    this.statisticInterface = statisticInterface;
    this.cloudMessageRepository = cloudMessageRepository;
  }

  public void virtualMachineStartTime(String user, VirtualMachine virtualMachine, long time) {

    final String cloudId = IdScopedByClouds.from(virtualMachine.id()).cloudId();

    Cloud cloud = null;
    try {
      cloud = cloudMessageRepository.getById(user, cloudId);
    } catch (Exception e) {
      LOGGER.warn("Could not retrieve cloud due to exception", e);
    }

    if (cloud == null) {
      //if we can't find the cloud, we skip the reporting
      LOGGER.warn(String.format(
          "Skipping statistics for virtual machine %s as cloud with id %s can not be found.",
          virtualMachine, cloudId));
      return;
    }

    final MetricBuilder metricBuilder = MetricBuilder.create().name("vm-start-time").value(time)
        .now().addTag("cloud", cloud.id())
        .addTag("api", cloud.api().providerName())
        .addTag("user", user);

    if (virtualMachine.hardwareId().isPresent()) {
      metricBuilder.addTag("hardware", virtualMachine.hardwareId().get());
    }
    if (virtualMachine.imageId().isPresent()) {
      metricBuilder.addTag("image", virtualMachine.imageId().get());
    }
    if (virtualMachine.locationId().isPresent()) {
      metricBuilder.addTag("location", virtualMachine.locationId().get());
    }

    final Metric metric = metricBuilder.build();

    LOGGER.debug(
        String.format("Reporting metric %s for virtual machine %s.", metric, virtualMachine));

    statisticInterface.reportMetric(metric);

  }

}
