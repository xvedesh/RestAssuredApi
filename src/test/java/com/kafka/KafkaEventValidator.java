package com.kafka;

import org.json.JSONObject;
import org.testng.Assert;
import com.utils.TestLogger;

public final class KafkaEventValidator {

    private KafkaEventValidator() {
    }

    public static void validateMetadata(ConsumedKafkaMessage message, String expectedTopic,
                                        String expectedClientId, String expectedEventType) {
        String metadataSummary = String.format(
                "expectedTopic=%s%nactualTopic=%s%nexpectedKey=%s%nactualKey=%s%nexpectedEventType=%s%nactualEventType=%s%nactualEntityType=%s%nactualClientId=%s%npartition=%d%noffset=%d",
                expectedTopic,
                message.getTopic(),
                expectedClientId,
                message.getKey(),
                expectedEventType,
                message.getEvent().getEventType(),
                message.getEvent().getEntityType(),
                message.getEvent().getClientId(),
                message.getPartition(),
                message.getOffset()
        );
        TestLogger.log("Validating Kafka metadata");
        TestLogger.attach(metadataSummary, "Kafka Metadata Assertions");
        Assert.assertEquals(message.getTopic(), expectedTopic, "Kafka topic mismatch");
        Assert.assertEquals(message.getKey(), expectedClientId, "Kafka key should match client id");
        Assert.assertEquals(message.getEvent().getEventType(), expectedEventType, "Kafka eventType mismatch");
        Assert.assertEquals(message.getEvent().getEntityType(), "CLIENT", "Kafka entityType mismatch");
        Assert.assertEquals(message.getEvent().getClientId(), expectedClientId, "Kafka clientId mismatch");
        Assert.assertNotNull(message.getEvent().getTimestamp(), "Kafka timestamp should be present");
        TestLogger.log("Kafka metadata validation passed");
    }

    public static void validatePayloadAgainstApiResponse(ConsumedKafkaMessage message, JSONObject apiResponse) {
        ClientEvent.ClientPayload payload = message.getEvent().getPayload();
        TestLogger.log("Validating Kafka payload against latest API response");
        TestLogger.attach(String.format(
                "apiResponse=%s%nkafkaPayload=%s",
                apiResponse.toString(2),
                message.getEvent().getPayload()
        ), "Kafka Payload Assertions");

        Assert.assertNotNull(payload, "Kafka payload should be present for non-delete events");
        Assert.assertEquals(payload.getId(), apiResponse.getString("id"), "Kafka payload id mismatch");
        Assert.assertEquals(payload.getFirstName(), apiResponse.getString("firstName"), "Kafka firstName mismatch");
        Assert.assertEquals(payload.getLastName(), apiResponse.getString("lastName"), "Kafka lastName mismatch");
        Assert.assertEquals(payload.getEmail(), apiResponse.getString("email"), "Kafka email mismatch");
        Assert.assertEquals(payload.getPhone(), apiResponse.getString("phone"), "Kafka phone mismatch");

        JSONObject address = apiResponse.getJSONObject("address");
        Assert.assertNotNull(payload.getAddress(), "Kafka payload address should be present");
        Assert.assertEquals(payload.getAddress().getStreet(), address.getString("street"), "Kafka street mismatch");
        Assert.assertEquals(payload.getAddress().getCity(), address.getString("city"), "Kafka city mismatch");
        Assert.assertEquals(payload.getAddress().getState(), address.getString("state"), "Kafka state mismatch");
        Assert.assertEquals(payload.getAddress().getZipCode(), address.getString("zipCode"), "Kafka zipCode mismatch");
        TestLogger.log(String.format(
                "Kafka payload validation passed for clientId=%s with payload=%s",
                message.getEvent().getClientId(),
                message.getEvent().getPayload()
        ));
    }

    public static void validateDeleteEvent(ConsumedKafkaMessage message) {
        TestLogger.log("Validating Kafka delete event payload is empty");
        TestLogger.attach(String.format(
                "clientId=%s%neventType=%s%npayload=%s",
                message.getEvent().getClientId(),
                message.getEvent().getEventType(),
                message.getEvent().getPayload()
        ), "Kafka Delete Assertions");
        Assert.assertTrue(message.getEvent().getPayload() == null,
                "Delete event payload should be empty to keep the contract minimal");
        TestLogger.log("Kafka delete event validation passed");
    }
}
