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

public class NodeAgent {

  private static final Injector INJECTOR = Guice
      .createInjector(new NodeModule(), new JpaModule("defaultPersistenceUnit", new JpaContext(
              Configuration.conf())), new MessageServiceModule(),
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())));

  private static final ExecutionService EXECUTION_SERVICE = new ScheduledThreadPoolExecutorExecutionService(
      new ScheduledThreadPoolExecutor(5));

  public static void main(String[] args) {
    INJECTOR.getInstance(NodeRequestListener.class).run();
    INJECTOR.getInstance(NodeQueryListener.class).run();
    INJECTOR.getInstance(NodeGroupQueryListener.class).run();
    EXECUTION_SERVICE.execute(INJECTOR.getInstance(NodeRequestWorker.class));
  }

}
