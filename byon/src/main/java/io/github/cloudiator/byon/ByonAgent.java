package io.github.cloudiator.byon;

import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

public final class ByonAgent {
  
  private static Injector injector =
      Guice.createInjector(new KafkaMessagingModule(), new MessageServiceModule());

  /**
   * the main method.
   * @param args args
   */
  public static void main(String[] args) {
    final AddByoNodeSubscriber createSubscriber =
        injector.getInstance(AddByoNodeSubscriber.class);
    createSubscriber.run();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        createSubscriber.terminate();
      }
    });
  }

}
