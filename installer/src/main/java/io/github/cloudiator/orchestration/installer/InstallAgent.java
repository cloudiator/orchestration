package io.github.cloudiator.orchestration.installer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class InstallAgent {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(InstallAgent.class);

  private static Injector injector =
      Guice.createInjector(new KafkaMessagingModule(), new MessageServiceModule());


  /**
   * starts the virtual machine agent.
   *
   * @param args args
   */
  public static void main(String[] args) {

    LOGGER.debug("Starting InstallAgent...");

    final NodeEventSubscriber nodeEventSubscriber =
        injector.getInstance(NodeEventSubscriber.class);
    nodeEventSubscriber.run();

  }

}
