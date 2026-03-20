Feature: Portfolio API to Kafka event validation

  @Kafka
  @KafkaPortfolioData
  @KafkaCreatePortfolio
  Scenario: Create portfolio publishes Kafka event
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "portfolio-events"
    When I create a new "portfolio" via API
    Then the API response status code for "portfolio" should be 201
    And I consume the entity Kafka event "PORTFOLIO_CREATED" from topic "portfolio-events" for "portfolio"
    And the consumed entity Kafka metadata for "portfolio" should match topic "portfolio-events" and event "PORTFOLIO_CREATED"
    And the consumed entity Kafka payload for "portfolio" should match the latest API response

  @Kafka
  @KafkaPortfolioData
  @KafkaPatchPortfolio
  Scenario: Patch portfolio publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a new "portfolio" via API
    And I start Kafka consumer for entity topic "portfolio-events"
    When I patch the current "portfolio" via PATCH
    Then the API response status code for "portfolio" should be 200
    And I consume the entity Kafka event "PORTFOLIO_PATCHED" from topic "portfolio-events" for "portfolio"
    And the consumed entity Kafka metadata for "portfolio" should match topic "portfolio-events" and event "PORTFOLIO_PATCHED"
    And the consumed entity Kafka payload for "portfolio" should match the latest API response

  @Kafka
  @KafkaPortfolioData
  @KafkaDeletePortfolio
  Scenario: Delete portfolio publishes Kafka event
    Given I send POST request to get Authorization Token
    And I create a new "portfolio" via API
    And I start Kafka consumer for entity topic "portfolio-events"
    When I delete the current "portfolio" via DELETE
    Then the API response status code for "portfolio" should be 200
    And I consume the entity Kafka event "PORTFOLIO_DELETED" from topic "portfolio-events" for "portfolio"
    And the consumed entity Kafka delete event for "portfolio" should match topic "portfolio-events"
