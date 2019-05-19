package io.github.cloudiator.iaas.byon;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.iaas.byon.config.ByonModule;
import io.github.cloudiator.persistance.JpaModule;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ByonAgent {

  private static final Injector INJECTOR = Guice
      .createInjector(new ByonModule(), new JpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())), new MessageServiceModule(),
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())));
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonAgent.class);

  /**
   * the main method.
   *
   * @param args args
   */
  public static void main(String[] args) {
    LOGGER.info("Using configuration: " + Configuration.conf());

    LOGGER.debug("Starting listeners.");
    LOGGER.debug("Starting " + AddByonNodeSubscriber.class);
    INJECTOR.getInstance(AddByonNodeSubscriber.class).run();

    LOGGER.debug("Finished starting listeners.");
  }
}
