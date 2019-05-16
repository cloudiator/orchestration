package io.github.cloudiator.iaas.byon;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ByonAgent {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonAgent.class);

  /**
   * the main method.
   *
   * @param args args
   */
  public static void main(String[] args) {
    LOGGER.error("implement main");
  }
}
