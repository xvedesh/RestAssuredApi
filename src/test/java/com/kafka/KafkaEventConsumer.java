package com.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utils.TestLogger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

public class KafkaEventConsumer implements AutoCloseable {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String topic;
    private final long pollIntervalMs;
    private final KafkaConsumer<String, String> consumer;

    public KafkaEventConsumer(String topic) {
        this.topic = topic;
        this.pollIntervalMs = KafkaConfig.getPollIntervalMs();
        this.consumer = new KafkaConsumer<>(buildProperties());
        assignTopicPartitions();
    }

    public ConsumedKafkaMessage waitForClientEvent(String expectedClientId, String expectedEventType, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        List<String> inspectedRecords = new ArrayList<>();

        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollIntervalMs));

            for (ConsumerRecord<String, String> record : records) {
                ClientEvent event = deserialize(record.value());
                String summary = String.format("key=%s,eventType=%s,offset=%d",
                        record.key(), event.getEventType(), record.offset());

                if (inspectedRecords.size() < 10) {
                    inspectedRecords.add(summary);
                }

                if (expectedClientId.equals(record.key()) && expectedEventType.equals(event.getEventType())) {
                    String consumedMessage = String.format(
                            "topic=%s%npartition=%d%noffset=%d%nkey=%s%neventType=%s%nrawPayload=%s",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            record.key(),
                            event.getEventType(),
                            record.value()
                    );
                    TestLogger.log("Consumed Kafka message for key=" + record.key() + " eventType=" + event.getEventType());
                    TestLogger.attach(consumedMessage, "Kafka Consumed Message");
                    return new ConsumedKafkaMessage(
                            record.topic(),
                            record.key(),
                            record.partition(),
                            record.offset(),
                            record.value(),
                            event
                    );
                }
            }
        }

        throw new AssertionError(String.format(
                "Timed out after %d ms waiting for Kafka event on topic '%s' with key='%s' and eventType='%s'. Inspected records: %s",
                timeoutMs,
                topic,
                expectedClientId,
                expectedEventType,
                inspectedRecords.isEmpty() ? "none" : inspectedRecords
        ));
    }

    public void assertNoClientEvent(String expectedClientId, String expectedEventType, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollIntervalMs));

            for (ConsumerRecord<String, String> record : records) {
                ClientEvent event = deserialize(record.value());
                if (expectedClientId.equals(record.key()) && expectedEventType.equals(event.getEventType())) {
                    throw new AssertionError(String.format(
                            "Unexpected Kafka event found on topic '%s' with key='%s' and eventType='%s'",
                            topic,
                            expectedClientId,
                            expectedEventType
                    ));
                }
            }
        }

        TestLogger.log(String.format(
                "Confirmed that no Kafka event was published for key=%s eventType=%s on topic=%s",
                expectedClientId,
                expectedEventType,
                topic
        ));
        TestLogger.attach(String.format(
                "topic=%s%nkey=%s%neventType=%s%ntimeoutMs=%d%nresult=no matching message consumed",
                topic,
                expectedClientId,
                expectedEventType,
                timeoutMs
        ), "Kafka Negative Assertions");
    }

    private Properties buildProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,
                KafkaConfig.getConsumerGroupPrefix() + "-" + UUID.randomUUID());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "client-events-consumer-" + UUID.randomUUID());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return properties;
    }

    private void assignTopicPartitions() {
        long deadline = System.currentTimeMillis() + KafkaConfig.getConsumerTimeoutMs();
        String lastFailure = "none";

        while (System.currentTimeMillis() < deadline) {
            try {
                List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic, Duration.ofMillis(pollIntervalMs));

                if (partitionInfos != null && !partitionInfos.isEmpty()) {
                    List<TopicPartition> partitions = partitionInfos.stream()
                            .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                            .collect(Collectors.toList());
                    consumer.assign(partitions);
                    consumer.seekToBeginning(partitions);
                    TestLogger.log("Kafka consumer assigned partitions " + partitions + " for topic " + topic);
                    TestLogger.attach("topic=" + topic + System.lineSeparator() + "partitions=" + partitions,
                            "Kafka Consumer Assignment");
                    return;
                }

                lastFailure = "topic metadata not available yet";
            } catch (TimeoutException timeoutException) {
                lastFailure = timeoutException.getMessage();
            }
            sleepQuietly();
        }

        throw new AssertionError(String.format(
                "Kafka consumer could not discover partitions for topic '%s' within %d ms. Last metadata error: %s",
                topic,
                KafkaConfig.getConsumerTimeoutMs(),
                lastFailure
        ));
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(pollIntervalMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for Kafka topic metadata", interruptedException);
        }
    }

    private ClientEvent deserialize(String payload) {
        try {
            return OBJECT_MAPPER.readValue(payload, ClientEvent.class);
        } catch (Exception exception) {
            throw new AssertionError("Failed to deserialize Kafka payload: " + payload, exception);
        }
    }

    @Override
    public void close() {
        consumer.close();
    }
}
