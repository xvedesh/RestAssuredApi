Feature: Account API to Kafka event validation

  @Kafka
  @KafkaAccountData
  @KafkaCreateAccount
  Scenario: Create account publishes Kafka event
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "account-events"
    When I create a new "account" via API
    Then the API response status code for "account" should be 201
    And I consume the entity Kafka event "ACCOUNT_CREATED" from topic "account-events" for "account"
    And the consumed entity Kafka metadata for "account" should match topic "account-events" and event "ACCOUNT_CREATED"
    And the consumed entity Kafka payload for "account" should match the latest API response

  @Kafka
  @KafkaAccountData
  @KafkaUpdateAccount
  Scenario: Update account publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a new "account" via API
    And I start Kafka consumer for entity topic "account-events"
    When I update the current "account" via PUT
    Then the API response status code for "account" should be 200
    And I consume the entity Kafka event "ACCOUNT_UPDATED" from topic "account-events" for "account"
    And the consumed entity Kafka metadata for "account" should match topic "account-events" and event "ACCOUNT_UPDATED"
    And the consumed entity Kafka payload for "account" should match the latest API response

  @Kafka
  @KafkaAccountData
  @KafkaDeleteAccount
  Scenario: Delete account publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a new "account" via API
    And I start Kafka consumer for entity topic "account-events"
    When I delete the current "account" via DELETE
    Then the API response status code for "account" should be 200
    And I consume the entity Kafka event "ACCOUNT_DELETED" from topic "account-events" for "account"
    And the consumed entity Kafka delete event for "account" should match topic "account-events"
