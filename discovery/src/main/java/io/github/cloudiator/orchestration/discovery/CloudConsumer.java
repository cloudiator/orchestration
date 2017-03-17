package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.cloudiator.messages.Cloud;
import org.cloudiator.messaging.kafka.Kafka;

import java.util.function.Function;

/**
 * Created by daniel on 15.03.17.
 */
public class CloudConsumer implements Runnable {

    private final Consumer<String, Cloud.CloudAdded> consumer =
        Kafka.Consumers.cloudAddedConsumer();
    private final CloudRegistry cloudRegistry;
    private final Function<Cloud.CloudAdded, de.uniulm.omi.cloudiator.sword.domain.Cloud>
        cloudConverter = new CloudAddedEventToCloud();

    public CloudConsumer(CloudRegistry cloudRegistry) {
        this.cloudRegistry = cloudRegistry;
    }

    @Override public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            final ConsumerRecords<String, Cloud.CloudAdded> poll = consumer.poll(1000);
            poll.forEach(record -> cloudRegistry.register(cloudConverter.apply(record.value())));
        }
    }
}
