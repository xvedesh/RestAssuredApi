Feature: Transaction API to Kafka event validation

  @Kafka
  @KafkaTransactionData
  @KafkaCreateTransaction
  Scenario: Create transaction publishes Kafka event
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "transaction-events"
    When I create a new "transaction" via API
    Then the API response status code for "transaction" should be 201
    And I consume the entity Kafka event "TRANSACTION_CREATED" from topic "transaction-events" for "transaction"
    And the consumed entity Kafka metadata for "transaction" should match topic "transaction-events" and event "TRANSACTION_CREATED"
    And the consumed entity Kafka payload for "transaction" should match the latest API response

  @Kafka
  @KafkaTransactionData
  @KafkaUpdateTransaction
  Scenario: Update transaction publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a new "transaction" via API
    And I start Kafka consumer for entity topic "transaction-events"
    When I update the current "transaction" via PUT
    Then the API response status code for "transaction" should be 200
    And I consume the entity Kafka event "TRANSACTION_UPDATED" from topic "transaction-events" for "transaction"
    And the consumed entity Kafka metadata for "transaction" should match topic "transaction-events" and event "TRANSACTION_UPDATED"
    And the consumed entity Kafka payload for "transaction" should match the latest API response

  @Kafka
  @KafkaTransactionData
  @KafkaDeleteTransaction
  Scenario: Delete transaction publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a new "transaction" via API
    And I start Kafka consumer for entity topic "transaction-events"
    When I delete the current "transaction" via DELETE
    Then the API response status code for "transaction" should be 200
    And I consume the entity Kafka event "TRANSACTION_DELETED" from topic "transaction-events" for "transaction"
    And the consumed entity Kafka delete event for "transaction" should match topic "transaction-events"
