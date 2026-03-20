package com.kafka;

import com.utils.ConfigurationReader;

import java.util.Optional;

public final class KafkaConfig {

    private KafkaConfig() {
    }

    public static String getBootstrapServers() {
        return resolve("kafkaBootstrapServers", "KAFKA_BOOTSTRAP_SERVERS");
    }

    public static String getClientEventsTopic() {
        return resolve("kafkaClientEventsTopic", "KAFKA_CLIENT_EVENTS_TOPIC");
    }

    public static String getAccountEventsTopic() {
        return resolve("kafkaAccountEventsTopic", "KAFKA_ACCOUNT_EVENTS_TOPIC");
    }

    public static String getPortfolioEventsTopic() {
        return resolve("kafkaPortfolioEventsTopic", "KAFKA_PORTFOLIO_EVENTS_TOPIC");
    }

    public static String getTransactionEventsTopic() {
        return resolve("kafkaTransactionEventsTopic", "KAFKA_TRANSACTION_EVENTS_TOPIC");
    }

    public static String getConsumerGroupPrefix() {
        return resolve("kafkaConsumerGroupPrefix", "KAFKA_CONSUMER_GROUP_PREFIX");
    }

    public static long getConsumerTimeoutMs() {
        return Long.parseLong(resolve("kafkaConsumerTimeoutMs", "KAFKA_CONSUMER_TIMEOUT_MS"));
    }

    public static long getPollIntervalMs() {
        return Long.parseLong(resolve("kafkaPollIntervalMs", "KAFKA_POLL_INTERVAL_MS"));
    }

    private static String resolve(String propertyKey, String envKey) {
        return Optional.ofNullable(System.getProperty(propertyKey))
                .orElseGet(() -> Optional.ofNullable(System.getenv(envKey))
                        .orElse(ConfigurationReader.getProperty(propertyKey)));
    }
}
