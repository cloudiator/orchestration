package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.domain.Resource;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.cloudiator.messages.Discovery;
import org.cloudiator.messaging.kafka.Kafka;

/**
 * Created by daniel on 26.01.17.
 */
public class KafkaDiscoveryReportingInterface implements DiscoveryReportingInterface {

    private final ResourceToDiscoveryEvent resourceToDiscoveryEvent =
        new ResourceToDiscoveryEvent();

    @Override public void report(Resource resource) {
        final Producer<String, Object> producer =
            Kafka.Producers.kafkaProducer();

        producer.send(new ProducerRecord<>(Kafka.Queue.DISCOVERY.queueName(),
            resourceToDiscoveryEvent.apply(resource)));

    }
}
