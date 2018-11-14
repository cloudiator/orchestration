package io.github.cloudiator.iaas.vm;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import de.uniulm.omi.cloudiator.util.statistics.Metric;
import de.uniulm.omi.cloudiator.util.statistics.MetricBuilder;
import de.uniulm.omi.cloudiator.util.statistics.StatisticInterface;
import io.github.cloudiator.persistance.CloudDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualMachineStatistics {

  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualMachineStatistics.class);

  private final StatisticInterface statisticInterface;
  private final CloudDomainRepository cloudDomainRepository;

  @Inject
  public VirtualMachineStatistics(
      StatisticInterface statisticInterface,
      CloudDomainRepository cloudDomainRepository) {
    this.statisticInterface = statisticInterface;
    this.cloudDomainRepository = cloudDomainRepository;
  }

  public void virtualMachineStartTime(VirtualMachine virtualMachine, long time) {

    final String cloudId = IdScopedByClouds.from(virtualMachine.id()).cloudId();
    final Cloud cloud = cloudDomainRepository.findById(cloudId);

    if (cloud == null) {
      //if we can't find the cloud, we skip the reporting
      LOGGER.warn(String.format(
          "Skipping statistics for virtual machine %s as cloud with id %s can not be found.",
          virtualMachine, cloudId));
      return;
    }

    final MetricBuilder metricBuilder = MetricBuilder.create().name("vm-start-time").value(time)
        .now().addTag("cloud", cloud.id())
        .addTag("api", cloud.api().providerName());

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
