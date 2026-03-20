Feature: Relationship and dependency validation

  @Negative
  @NegativeAccount
  Scenario: Create account with non-existing clientId should fail
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "account-events"
    When I create an account with non-existing clientId
    Then the API error response for "account" should be 400 and contain "Client not found for clientId="
    And no entity Kafka event "ACCOUNT_CREATED" should be published for "account" within 3000 ms

  @Negative
  @NegativeAccount
  Scenario: Update account with non-existing clientId should fail
    Given I send POST request to get Authorization Token
    And I create a new "account" via API
    And I start Kafka consumer for entity topic "account-events"
    When I update the current account with non-existing clientId
    Then the API error response for "account" should be 400 and contain "Client not found for clientId="
    And no entity Kafka event "ACCOUNT_UPDATED" should be published for "account" within 3000 ms

  @Negative
  @NegativePortfolio
  Scenario: Create portfolio with non-existing clientId should fail
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "portfolio-events"
    When I create a portfolio with non-existing clientId
    Then the API error response for "portfolio" should be 400 and contain "Client not found for clientId="
    And no entity Kafka event "PORTFOLIO_CREATED" should be published for "portfolio" within 3000 ms

  @Negative
  @NegativePortfolio
  Scenario: Patch portfolio with non-existing clientId should fail
    Given I send POST request to get Authorization Token
    And I create a new "portfolio" via API
    And I start Kafka consumer for entity topic "portfolio-events"
    When I patch the current portfolio with non-existing clientId
    Then the API error response for "portfolio" should be 400 and contain "Client not found for clientId="
    And no entity Kafka event "PORTFOLIO_PATCHED" should be published for "portfolio" within 3000 ms

  @Negative
  @NegativeTransaction
  Scenario: Create transaction with non-existing clientId should fail
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "transaction-events"
    When I create a transaction with non-existing clientId
    Then the API error response for "transaction" should be 400 and contain "Client not found for clientId="
    And no entity Kafka event "TRANSACTION_CREATED" should be published for "transaction" within 3000 ms

  @Negative
  @NegativeTransaction
  Scenario: Create transaction with non-existing accountId should fail
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "transaction-events"
    When I create a transaction with non-existing accountId
    Then the API error response for "transaction" should be 400 and contain "Account not found for accountId="
    And no entity Kafka event "TRANSACTION_CREATED" should be published for "transaction" within 3000 ms

  @Negative
  @NegativeTransaction
  Scenario: Create transaction with mismatched clientId and account owner should fail
    Given I send POST request to get Authorization Token
    And I start Kafka consumer for entity topic "transaction-events"
    When I create a transaction with mismatched clientId and account owner
    Then the API error response for "transaction" should be 400 and contain "Transaction clientId does not match account owner"
    And no entity Kafka event "TRANSACTION_CREATED" should be published for "transaction" within 3000 ms

  @Negative
  @NegativeTransaction
  Scenario: Update transaction to invalid account and client relation should fail
    Given I send POST request to get Authorization Token
    And I create a new "transaction" via API
    And I start Kafka consumer for entity topic "transaction-events"
    When I update the current transaction with invalid account and client relationship
    Then the API error response for "transaction" should be 400 and contain "Transaction clientId does not match account owner"
    And no entity Kafka event "TRANSACTION_UPDATED" should be published for "transaction" within 3000 ms

  @Negative
  @NegativeDelete
  Scenario: Delete client with existing accounts should fail
    Given I send POST request to get Authorization Token
    And I create a new client with a dependent account
    And I start Kafka consumer for client events
    When I delete the current client via DELETE
    Then the API error response for "client" should be 409 and contain "Cannot delete client with existing accounts"
    And no Kafka client event "CLIENT_DELETED" should be published for the current client within 3000 ms

  @Negative
  @NegativeDelete
  Scenario: Delete client with existing portfolios should fail
    Given I send POST request to get Authorization Token
    And I create a new client with a dependent portfolio
    And I start Kafka consumer for client events
    When I delete the current client via DELETE
    Then the API error response for "client" should be 409 and contain "Cannot delete client with existing portfolios"
    And no Kafka client event "CLIENT_DELETED" should be published for the current client within 3000 ms

  @Negative
  @NegativeDelete
  Scenario: Delete client with existing transactions should fail
    Given I send POST request to get Authorization Token
    And I create a new client with a dependent transaction
    And I start Kafka consumer for client events
    When I delete the current client via DELETE
    Then the API error response for "client" should be 409 and contain "Cannot delete client with existing transactions"
    And no Kafka client event "CLIENT_DELETED" should be published for the current client within 3000 ms

  @Negative
  @NegativeDelete
  Scenario: Delete account with existing transactions should fail
    Given I send POST request to get Authorization Token
    And I create a client via API
    And I create a new account with a dependent transaction for the current client
    And I start Kafka consumer for entity topic "account-events"
    When I delete the current "account" via DELETE
    Then the API error response for "account" should be 409 and contain "Cannot delete account with existing transactions"
    And no entity Kafka event "ACCOUNT_DELETED" should be published for "account" within 3000 ms
