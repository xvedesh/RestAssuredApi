package com.step_defs;

import com.api.BaseTest;
import com.api.ClientAPI;
import com.kafka.ConsumedKafkaMessage;
import com.kafka.KafkaConfig;
import com.kafka.KafkaEventValidator;
import com.kafka.ScenarioContext;
import com.utils.TestLogger;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class ClientKafkaStepDefs {
    private final ClientAPI clientAPI = new ClientAPI();

    @Given("I start Kafka consumer for client events")
    public void i_start_kafka_consumer_for_client_events() {
        TestLogger.log("Starting Kafka consumer for client events");
        ScenarioContext.startKafkaConsumer();
    }

    @When("I create a client via API")
    public void i_create_a_client_via_api() {
        TestLogger.log("Executing API action: create client");
        clientAPI.post();
    }

    @When("I update the current client via PUT")
    public void i_update_the_current_client_via_put() {
        TestLogger.log("Executing API action: update client via PUT");
        clientAPI.put();
    }

    @When("I patch the current client address via PATCH")
    public void i_patch_the_current_client_address_via_patch() {
        TestLogger.log("Executing API action: patch client address via PATCH");
        clientAPI.patch();
    }

    @When("I delete the current client via DELETE")
    public void i_delete_the_current_client_via_delete() {
        TestLogger.log("Executing API action: delete client via DELETE");
        clientAPI.delete();
    }

    @Then("the API response status code should be {int}")
    public void the_api_response_status_code_should_be(Integer expectedStatusCode) {
        Assert.assertNotNull(ClientAPI.getLatestResponse(), "No API response is available for validation");
        TestLogger.attach(String.format(
                "expectedStatusCode=%d%nactualStatusCode=%d%nresponseBody=%s",
                expectedStatusCode,
                ClientAPI.getLatestResponse().statusCode(),
                ClientAPI.getLatestResponse().getBody().asPrettyString()
        ), "HTTP Response Assertions");
        TestLogger.log(String.format(
                "Validating HTTP status code: expected=%d, actual=%d",
                expectedStatusCode,
                ClientAPI.getLatestResponse().statusCode()
        ));
        Assert.assertEquals(ClientAPI.getLatestResponse().statusCode(), expectedStatusCode.intValue(),
                "Unexpected HTTP status code");
        TestLogger.log("HTTP status code validation passed");
    }

    @Then("I consume the Kafka client event {string} for the current client")
    public void i_consume_the_kafka_client_event_for_the_current_client(String expectedEventType) {
        TestLogger.log(String.format(
                "Waiting for Kafka event eventType=%s clientId=%s topic=%s timeoutMs=%d",
                expectedEventType,
                ClientAPI.getCurrentClientId(),
                KafkaConfig.getClientEventsTopic(),
                KafkaConfig.getConsumerTimeoutMs()
        ));
        ConsumedKafkaMessage message = ScenarioContext.getKafkaConsumer()
                .waitForClientEvent(
                        ClientAPI.getCurrentClientId(),
                        expectedEventType,
                        KafkaConfig.getConsumerTimeoutMs()
                );
        ScenarioContext.setConsumedKafkaMessage(message);
    }

    @Then("the consumed Kafka event metadata should match the current client and topic")
    public void the_consumed_kafka_event_metadata_should_match_the_current_client_and_topic() {
        KafkaEventValidator.validateMetadata(
                ScenarioContext.getConsumedKafkaMessage(),
                KafkaConfig.getClientEventsTopic(),
                ClientAPI.getCurrentClientId(),
                ScenarioContext.getConsumedKafkaMessage().getEvent().getEventType()
        );
    }

    @Then("the consumed Kafka payload should match the latest client API response")
    public void the_consumed_kafka_payload_should_match_the_latest_client_api_response() {
        KafkaEventValidator.validatePayloadAgainstApiResponse(
                ScenarioContext.getConsumedKafkaMessage(),
                ClientAPI.getLatestResponseBodyAsJsonObject()
        );
    }

    @Then("the consumed Kafka delete event should match the current client")
    public void the_consumed_kafka_delete_event_should_match_the_current_client() {
        KafkaEventValidator.validateMetadata(
                ScenarioContext.getConsumedKafkaMessage(),
                KafkaConfig.getClientEventsTopic(),
                ClientAPI.getCurrentClientId(),
                "CLIENT_DELETED"
        );
        KafkaEventValidator.validateDeleteEvent(ScenarioContext.getConsumedKafkaMessage());
    }

    @Then("no Kafka client event {string} should be published for the current client within {int} ms")
    public void no_kafka_client_event_should_be_published_for_the_current_client_within_ms(String eventType, Integer timeoutMs) {
        ScenarioContext.getKafkaConsumer()
                .assertNoClientEvent(ClientAPI.getCurrentClientId(), eventType, timeoutMs.longValue());
    }
}
