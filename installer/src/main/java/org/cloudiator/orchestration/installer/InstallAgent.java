package org.cloudiator.orchestration.installer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class InstallAgent {

  private static Injector injector =
      Guice.createInjector(new KafkaMessagingModule(), new MessageServiceModule());


  /**
   * starts the virtual machine agent.
   *
   * @param args args
   */
  public static void main(String[] args) {
    final NodeEventSubscriber nodeEventSubscriber =
        injector.getInstance(NodeEventSubscriber.class);
    nodeEventSubscriber.run();

  }

}
