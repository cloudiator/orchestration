package io.github.cloudiator.iaas.vm;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
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
          new VmAgentModule());

  /**
   * starts the virtual machine agent.
   *
   * @param args args
   */
  public static void main(String[] args) {
    final CreateVirtualMachineSubscriber createSubscriber =
        injector.getInstance(CreateVirtualMachineSubscriber.class);
    createSubscriber.run();
    EXECUTION_SERVICE.execute(injector.getInstance(VirtualMachineRequestQueueWorker.class));

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        //createSubscriber.terminate();
      }
    });
  }
}
