package io.github.cloudiator.iaas.vm;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;
import de.uniulm.omi.cloudiator.sword.multicloud.service.IdScopedByClouds;
import de.uniulm.omi.cloudiator.util.statistics.MetricBuilder;
import de.uniulm.omi.cloudiator.util.statistics.StatisticInterface;
import io.github.cloudiator.persistance.CloudDomainRepository;

public class VirtualMachineStatistics {

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

    statisticInterface.reportMetric(metricBuilder.build());

  }

}
