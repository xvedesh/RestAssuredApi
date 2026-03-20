Feature: Cross-entity business flow validation

  @BusinessFlow
  @CrossEntityE2E
  Scenario: Create client account and transaction with full Kafka validation
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for client events
    When I create a client via API
    Then the API response status code should be 201
    And I consume the Kafka client event "CLIENT_CREATED" for the current client
    And the consumed Kafka event metadata should match the current client and topic
    And the consumed Kafka payload should match the latest client API response
    And I start Kafka consumer for entity topic "account-events"
    And I prepare the current account for the current client
    When I create a new "account" via API
    Then the API response status code for "account" should be 201
    And I consume the entity Kafka event "ACCOUNT_CREATED" from topic "account-events" for "account"
    And the consumed entity Kafka metadata for "account" should match topic "account-events" and event "ACCOUNT_CREATED"
    And the consumed entity Kafka payload for "account" should match the latest API response
    And I start Kafka consumer for entity topic "transaction-events"
    And I prepare the current transaction for the current client and account
    When I create a new "transaction" via API
    Then the API response status code for "transaction" should be 201
    And I consume the entity Kafka event "TRANSACTION_CREATED" from topic "transaction-events" for "transaction"
    And the consumed entity Kafka metadata for "transaction" should match topic "transaction-events" and event "TRANSACTION_CREATED"
    And the consumed entity Kafka payload for "transaction" should match the latest API response
    And the current account should reference the current client
    And the current transaction should reference the current client and account
