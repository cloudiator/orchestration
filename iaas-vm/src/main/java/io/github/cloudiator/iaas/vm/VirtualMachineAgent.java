package io.github.cloudiator.iaas.vm;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import io.github.cloudiator.iaas.vm.config.VmAgentModule;
import io.github.cloudiator.persistance.JpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class VirtualMachineAgent {

  private static final ExecutionService EXECUTION_SERVICE = new ScheduledThreadPoolExecutorExecutionService(
      new LoggingScheduledThreadPoolExecutor(5));

  private static Injector injector =
      Guice.createInjector(
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())),
          new MessageServiceModule(),
          new JpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())),
          new VmAgentModule());

  /**
   * starts the virtual machine agent.
   *
   * @param args args
   */
  public static void main(String[] args) {

    EXECUTION_SERVICE.execute(injector.getInstance(VirtualMachineRequestQueueWorker.class));

    injector.getInstance(CloudCreatedSubscriber.class).run();
    injector.getInstance(CreateVirtualMachineSubscriber.class).run();
    injector.getInstance(VirtualMachineQuerySubscriber.class).run();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        //createSubscriber.terminate();
      }
    });
  }
}
