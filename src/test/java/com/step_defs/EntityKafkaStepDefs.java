package com.step_defs;

import com.api.APIMap;
import com.interfaces.PayLoadValidator;
import com.kafka.ConsumedEntityKafkaMessage;
import com.kafka.EntityKafkaEventValidator;
import com.kafka.KafkaConfig;
import com.kafka.ScenarioContext;
import com.utils.TestLogger;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class EntityKafkaStepDefs {

    @Given("I start Kafka consumer for entity topic {string}")
    public void i_start_kafka_consumer_for_entity_topic(String topic) {
        TestLogger.log("Starting Kafka consumer for entity topic " + topic);
        ScenarioContext.startEntityKafkaConsumer(topic);
    }

    @When("I create a new {string} via API")
    public void i_create_a_new_via_api(String block) {
        TestLogger.log("Executing API action: create " + block);
        APIMap.getApiValidator(block).post();
    }

    @When("I update the current {string} via PUT")
    public void i_update_the_current_via_put(String block) {
        TestLogger.log("Executing API action: update " + block + " via PUT");
        APIMap.getApiValidator(block).put();
    }

    @When("I patch the current {string} via PATCH")
    public void i_patch_the_current_via_patch(String block) {
        TestLogger.log("Executing API action: patch " + block + " via PATCH");
        APIMap.getApiValidator(block).patch();
    }

    @When("I delete the current {string} via DELETE")
    public void i_delete_the_current_via_delete(String block) {
        TestLogger.log("Executing API action: delete " + block + " via DELETE");
        APIMap.getApiValidator(block).delete();
    }

    @Then("the API response status code for {string} should be {int}")
    public void the_api_response_status_code_for_should_be(String block, Integer expectedStatusCode) {
        PayLoadValidator validator = APIMap.getApiValidator(block);
        Assert.assertNotNull(validator.fetchLatestResponse(), "No API response is available for validation");
        TestLogger.attach(String.format(
                "entity=%s%nexpectedStatusCode=%d%nactualStatusCode=%d%nresponseBody=%s",
                block,
                expectedStatusCode,
                validator.fetchLatestResponse().statusCode(),
                validator.fetchLatestResponse().getBody().asPrettyString()
        ), "HTTP Response Assertions");
        Assert.assertEquals(validator.fetchLatestResponse().statusCode(), expectedStatusCode.intValue(),
                "Unexpected HTTP status code");
        TestLogger.log("HTTP status code validation passed for " + block);
    }

    @Then("I consume the entity Kafka event {string} from topic {string} for {string}")
    public void i_consume_the_entity_kafka_event_from_topic_for(String expectedEventType, String topic, String block) {
        PayLoadValidator validator = APIMap.getApiValidator(block);
        TestLogger.log(String.format(
                "Waiting for Kafka event eventType=%s entityId=%s topic=%s timeoutMs=%d",
                expectedEventType,
                validator.getCurrentEntityId(),
                topic,
                KafkaConfig.getConsumerTimeoutMs()
        ));
        ConsumedEntityKafkaMessage message = ScenarioContext.getEntityKafkaConsumer()
                .waitForEntityEvent(
                        validator.getCurrentEntityId(),
                        expectedEventType,
                        KafkaConfig.getConsumerTimeoutMs()
                );
        ScenarioContext.setConsumedEntityKafkaMessage(message);
    }

    @Then("the consumed entity Kafka metadata for {string} should match topic {string} and event {string}")
    public void the_consumed_entity_kafka_metadata_for_should_match_topic_and_event(String block, String topic, String eventType) {
        PayLoadValidator validator = APIMap.getApiValidator(block);
        EntityKafkaEventValidator.validateMetadata(
                ScenarioContext.getConsumedEntityKafkaMessage(),
                topic,
                validator.getCurrentEntityId(),
                eventType,
                validator.getEntityType()
        );
    }

    @Then("the consumed entity Kafka payload for {string} should match the latest API response")
    public void the_consumed_entity_kafka_payload_for_should_match_the_latest_api_response(String block) {
        PayLoadValidator validator = APIMap.getApiValidator(block);
        EntityKafkaEventValidator.validatePayloadAgainstApiResponse(
                ScenarioContext.getConsumedEntityKafkaMessage(),
                validator.fetchLatestResponseBodyAsJsonObject()
        );
    }

    @Then("the consumed entity Kafka delete event for {string} should match topic {string}")
    public void the_consumed_entity_kafka_delete_event_for_should_match_topic(String block, String topic) {
        PayLoadValidator validator = APIMap.getApiValidator(block);
        EntityKafkaEventValidator.validateMetadata(
                ScenarioContext.getConsumedEntityKafkaMessage(),
                topic,
                validator.getCurrentEntityId(),
                ScenarioContext.getConsumedEntityKafkaMessage().getEvent().getEventType(),
                validator.getEntityType()
        );
        EntityKafkaEventValidator.validateDeleteEvent(ScenarioContext.getConsumedEntityKafkaMessage());
    }

    @Then("no entity Kafka event {string} should be published for {string} within {int} ms")
    public void no_entity_kafka_event_should_be_published_for_within_ms(String expectedEventType, String block, Integer timeoutMs) {
        PayLoadValidator validator = APIMap.getApiValidator(block);
        ScenarioContext.getEntityKafkaConsumer()
                .assertNoEntityEvent(
                        validator.getCurrentEntityId(),
                        expectedEventType,
                        timeoutMs.longValue()
                );
    }
}
