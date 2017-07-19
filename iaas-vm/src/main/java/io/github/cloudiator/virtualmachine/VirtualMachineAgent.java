package io.github.cloudiator.virtualmachine;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class VirtualMachineAgent {

  private static Injector injector =
      Guice.createInjector(new KafkaMessagingModule(), new MessageServiceModule());

  /**
   * starts the virtual machine agent.
   * 
   * @param args args
   */
  public static void main(String[] args) {
    final CreateVirtualMachineSubscriberOld createSubscriber =
        injector.getInstance(CreateVirtualMachineSubscriberOld.class);
    createSubscriber.run();
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        createSubscriber.terminate();
      }
    });
  }
}
