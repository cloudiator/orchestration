package io.github.cloudiator.noderegistry;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class NodeRegistryAgent {

  private static Injector injector =
      Guice.createInjector(new KafkaMessagingModule(), new MessageServiceModule(),
          new NodeRegistryModule());

  /**
   * the main method.
   *
   * @param args args
   */
  public static void main(String[] args) {
    final NodeRegistrySubscriber createSubscriber =
        injector.getInstance(NodeRegistrySubscriber.class);
    createSubscriber.run();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        createSubscriber.terminate();
      }
    });
  }

  private static class NodeRegistryModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(NodeRegistry.class).to(NodeRegistryFileImpl.class).in(Singleton.class);
    }
  }

}
