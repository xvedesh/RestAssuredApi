package com.kafka;

public final class ScenarioContext {
    private static final ThreadLocal<KafkaEventConsumer> kafkaConsumer = new ThreadLocal<>();
    private static final ThreadLocal<ConsumedKafkaMessage> consumedKafkaMessage = new ThreadLocal<>();
    private static final ThreadLocal<EntityKafkaEventConsumer> entityKafkaConsumer = new ThreadLocal<>();
    private static final ThreadLocal<ConsumedEntityKafkaMessage> consumedEntityKafkaMessage = new ThreadLocal<>();

    private ScenarioContext() {
    }

    public static void startKafkaConsumer() {
        clearConsumerOnly();
        kafkaConsumer.set(new KafkaEventConsumer(KafkaConfig.getClientEventsTopic()));
    }

    public static KafkaEventConsumer getKafkaConsumer() {
        KafkaEventConsumer consumer = kafkaConsumer.get();
        if (consumer == null) {
            throw new IllegalStateException("Kafka consumer has not been started for this scenario");
        }
        return consumer;
    }

    public static void setConsumedKafkaMessage(ConsumedKafkaMessage message) {
        consumedKafkaMessage.set(message);
    }

    public static ConsumedKafkaMessage getConsumedKafkaMessage() {
        ConsumedKafkaMessage message = consumedKafkaMessage.get();
        if (message == null) {
            throw new IllegalStateException("No Kafka event has been consumed for this scenario");
        }
        return message;
    }

    public static void startEntityKafkaConsumer(String topic) {
        clearEntityConsumerOnly();
        entityKafkaConsumer.set(new EntityKafkaEventConsumer(topic));
    }

    public static EntityKafkaEventConsumer getEntityKafkaConsumer() {
        EntityKafkaEventConsumer consumer = entityKafkaConsumer.get();
        if (consumer == null) {
            throw new IllegalStateException("Entity Kafka consumer has not been started for this scenario");
        }
        return consumer;
    }

    public static void setConsumedEntityKafkaMessage(ConsumedEntityKafkaMessage message) {
        consumedEntityKafkaMessage.set(message);
    }

    public static ConsumedEntityKafkaMessage getConsumedEntityKafkaMessage() {
        ConsumedEntityKafkaMessage message = consumedEntityKafkaMessage.get();
        if (message == null) {
            throw new IllegalStateException("No entity Kafka event has been consumed for this scenario");
        }
        return message;
    }

    public static void clear() {
        clearConsumerOnly();
        clearEntityConsumerOnly();
        consumedKafkaMessage.remove();
        consumedEntityKafkaMessage.remove();
    }

    private static void clearConsumerOnly() {
        KafkaEventConsumer consumer = kafkaConsumer.get();
        if (consumer != null) {
            consumer.close();
            kafkaConsumer.remove();
        }
    }

    private static void clearEntityConsumerOnly() {
        EntityKafkaEventConsumer consumer = entityKafkaConsumer.get();
        if (consumer != null) {
            consumer.close();
            entityKafkaConsumer.remove();
        }
    }
}
