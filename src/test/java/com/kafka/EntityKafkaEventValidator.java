package com.kafka;

import com.utils.TestLogger;
import org.json.JSONObject;
import org.testng.Assert;

public final class EntityKafkaEventValidator {

    private EntityKafkaEventValidator() {
    }

    public static void validateMetadata(ConsumedEntityKafkaMessage message, String expectedTopic,
                                        String expectedEntityId, String expectedEventType, String expectedEntityType) {
        String metadataSummary = String.format(
                "expectedTopic=%s%nactualTopic=%s%nexpectedKey=%s%nactualKey=%s%nexpectedEventType=%s%nactualEventType=%s%nexpectedEntityType=%s%nactualEntityType=%s%nexpectedEntityId=%s%nactualEntityId=%s%npartition=%d%noffset=%d",
                expectedTopic,
                message.getTopic(),
                expectedEntityId,
                message.getKey(),
                expectedEventType,
                message.getEvent().getEventType(),
                expectedEntityType,
                message.getEvent().getEntityType(),
                expectedEntityId,
                message.getEvent().getEntityId(),
                message.getPartition(),
                message.getOffset()
        );
        TestLogger.log("Validating entity Kafka metadata");
        TestLogger.attach(metadataSummary, "Kafka Metadata Assertions");

        Assert.assertEquals(message.getTopic(), expectedTopic, "Kafka topic mismatch");
        Assert.assertEquals(message.getKey(), expectedEntityId, "Kafka key should match entity id");
        Assert.assertEquals(message.getEvent().getEventType(), expectedEventType, "Kafka eventType mismatch");
        Assert.assertEquals(message.getEvent().getEntityType(), expectedEntityType, "Kafka entityType mismatch");
        Assert.assertEquals(message.getEvent().getEntityId(), expectedEntityId, "Kafka entityId mismatch");
        Assert.assertNotNull(message.getEvent().getTimestamp(), "Kafka timestamp should be present");
        TestLogger.log("Entity Kafka metadata validation passed");
    }

    public static void validatePayloadAgainstApiResponse(ConsumedEntityKafkaMessage message, JSONObject apiResponse) {
        TestLogger.log("Validating entity Kafka payload against latest API response");

        JSONObject payload = message.getEvent().getPayload() == null
                ? null
                : new JSONObject(message.getEvent().getPayload());

        TestLogger.attach(String.format(
                "apiResponse=%s%nkafkaPayload=%s",
                apiResponse.toString(2),
                payload == null ? "null" : payload.toString(2)
        ), "Kafka Payload Assertions");

        Assert.assertNotNull(payload, "Kafka payload should be present for non-delete events");
        Assert.assertTrue(payload.similar(apiResponse),
                "Kafka payload should match API response body");
        TestLogger.log("Entity Kafka payload validation passed");
    }

    public static void validateDeleteEvent(ConsumedEntityKafkaMessage message) {
        TestLogger.log("Validating entity Kafka delete event payload is empty");
        TestLogger.attach(String.format(
                "entityId=%s%neventType=%s%npayload=%s",
                message.getEvent().getEntityId(),
                message.getEvent().getEventType(),
                message.getEvent().getPayload()
        ), "Kafka Delete Assertions");
        Assert.assertTrue(message.getEvent().getPayload() == null,
                "Delete event payload should be empty");
        TestLogger.log("Entity Kafka delete validation passed");
    }
}
