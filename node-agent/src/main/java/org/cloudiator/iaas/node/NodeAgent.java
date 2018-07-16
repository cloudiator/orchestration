package org.cloudiator.iaas.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import io.github.cloudiator.persistance.JpaModule;
import io.github.cloudiator.util.JpaContext;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.cloudiator.iaas.node.config.NodeModule;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeAgent {

  private static final Injector INJECTOR = Guice
      .createInjector(new NodeModule(), new JpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())), new MessageServiceModule(),
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())));
  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeAgent.class);

  private static final ExecutionService EXECUTION_SERVICE = new ScheduledThreadPoolExecutorExecutionService(
      new ScheduledThreadPoolExecutor(5));

  public static void main(String[] args) {

    LOGGER.debug("Starting listeners.");
    LOGGER.debug("Starting " + NodeRequestListener.class);
    INJECTOR.getInstance(NodeRequestListener.class).run();
    LOGGER.debug("Starting " + NodeQueryListener.class);
    INJECTOR.getInstance(NodeQueryListener.class).run();
    LOGGER.debug("Starting " + NodeGroupQueryListener.class);
    INJECTOR.getInstance(NodeGroupQueryListener.class).run();
    LOGGER.debug("Starting " + NodeRequestWorker.class);
    EXECUTION_SERVICE.execute(INJECTOR.getInstance(NodeRequestWorker.class));
    LOGGER.debug("Finished starting listeners.");
  }

}
