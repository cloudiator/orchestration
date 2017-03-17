package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudBuilder;
import de.uniulm.omi.cloudiator.sword.multicloud.MultiCloudService;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import org.cloudiator.messaging.KafkaMessageInterface;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.kafka.Kafka.Queue;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryAgent {

    private static final ExecutionService EXECUTION_SERVICE =
        new ScheduledThreadPoolExecutorExecutionService(new LoggingScheduledThreadPoolExecutor(10));

    public static void main(String[] args) {

        final MultiCloudService multiCloudService = MultiCloudBuilder.newBuilder().build();

        final ResourceSupplier resourceSupplier = new NewResourceSupplier(
            new ResourceSuppliers(multiCloudService.computeService().discoveryService())
                .imageSupplier(), new SetBasedDiscoveryStore());
        final DiscoveryQueue discoveryQueue = new DiscoveryQueue();

        EXECUTION_SERVICE.schedule(new DiscoveryWorker(resourceSupplier, discoveryQueue));
        EXECUTION_SERVICE
            .execute(new DiscoveryReporter(new KafkaDiscoveryReportingInterface(), discoveryQueue));

        MessageInterface messageInterface = new KafkaMessageInterface();
        messageInterface
            .subscribe(Queue.CLOUD.queueName(), org.cloudiator.messages.Cloud.CloudAdded.parser(),
                cloudAdded -> multiCloudService.cloudRegistry()
                    .register(new CloudAddedEventToCloud().apply(cloudAdded)));
    }

}
