Feature: Client API to Kafka event validation

  @KafkaClientData
  @KafkaCreateClient
  Scenario: Create client publishes Kafka event
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for client events
    When I create a client via API
    Then the API response status code should be 201
    And I consume the Kafka client event "CLIENT_CREATED" for the current client
    And the consumed Kafka event metadata should match the current client and topic
    And the consumed Kafka payload should match the latest client API response

  @KafkaClientData
  @KafkaUpdateClient
  Scenario: Update client publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a client via API
    And I start Kafka consumer for client events
    When I update the current client via PUT
    Then the API response status code should be 200
    And I consume the Kafka client event "CLIENT_UPDATED" for the current client
    And the consumed Kafka event metadata should match the current client and topic
    And the consumed Kafka payload should match the latest client API response

  @KafkaClientData
  @KafkaPatchClient
  Scenario: Patch client publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a client via API
    And I start Kafka consumer for client events
    When I patch the current client address via PATCH
    Then the API response status code should be 200
    And I consume the Kafka client event "CLIENT_PATCHED" for the current client
    And the consumed Kafka event metadata should match the current client and topic
    And the consumed Kafka payload should match the latest client API response

  @KafkaClientData
  @KafkaDeleteClient
  Scenario: Delete client publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a client via API
    And I start Kafka consumer for client events
    When I delete the current client via DELETE
    Then the API response status code should be 200
    And I consume the Kafka client event "CLIENT_DELETED" for the current client
    And the consumed Kafka delete event should match the current client
