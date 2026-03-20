# AI Kafka Validator Framework Data Flow

## High-Level Execution Map

```text
Command (mvn test OR docker compose up)
  -> pom.xml (Surefire reads ${testng.suite.file})
  -> testng.xml / testng-rerun.xml
  -> com.runners.CukesRunner or com.runners.FailedTestRunner
  -> Cucumber loads feature files and glue (com.step_defs)
  -> step definitions call API classes
  -> API classes send HTTP requests to JSON server
  -> Node server validates business rules and writes clients.json
  -> Node server publishes Kafka events for successful operations
  -> Java Kafka consumers read matching events
  -> validators compare API response and Kafka payload
  -> Cucumber/TestNG/Surefire write reports to target/
```

## Main Runtime Components

### Maven / TestNG / Cucumber

- [pom.xml](/Users/vivedesh/ai-kafka-validator/pom.xml)
  Surefire launches the TestNG suite configured by `-Dtestng.suite.file`
- [testng.xml](/Users/vivedesh/ai-kafka-validator/testng.xml)
  Main parallel suite using `com.runners.CukesRunner`
- [testng-rerun.xml](/Users/vivedesh/ai-kafka-validator/testng-rerun.xml)
  Failed-scenario rerun suite using `com.runners.FailedTestRunner`
- [src/test/java/com/runners/CukesRunner.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/runners/CukesRunner.java)
  Main Cucumber runner
- [src/test/java/com/runners/FailedTestRunner.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/runners/FailedTestRunner.java)
  Rerun-only runner for `@target/rerun.txt`

### Step Definitions

- [src/test/java/com/step_defs/ClientDataStepDefs.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/step_defs/ClientDataStepDefs.java)
  CRUD regression orchestration
- [src/test/java/com/step_defs/ClientKafkaStepDefs.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/step_defs/ClientKafkaStepDefs.java)
  Client Kafka validation
- [src/test/java/com/step_defs/EntityKafkaStepDefs.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/step_defs/EntityKafkaStepDefs.java)
  Account, portfolio, transaction Kafka validation
- [src/test/java/com/step_defs/BusinessValidationStepDefs.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/step_defs/BusinessValidationStepDefs.java)
  Relationship, negative, and chained business-flow steps
- [src/test/java/com/step_defs/Hooks.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/step_defs/Hooks.java)
  Scenario-level logging and thread-local cleanup

### API Layer

- [src/test/java/com/api/BaseTest.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/api/BaseTest.java)
  Base auth and header handling
- [src/test/java/com/api/APIMap.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/api/APIMap.java)
  Maps feature `block` names to API implementations
- [src/test/java/com/api/ClientAPI.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/api/ClientAPI.java)
- [src/test/java/com/api/AccountAPI.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/api/AccountAPI.java)
- [src/test/java/com/api/PortfolioAPI.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/api/PortfolioAPI.java)
- [src/test/java/com/api/TransactionAPI.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/api/TransactionAPI.java)

### Kafka Layer

- [server/kafkaPublisher.js](/Users/vivedesh/ai-kafka-validator/server/kafkaPublisher.js)
  Producer connection and event publishing
- [src/test/java/com/kafka/KafkaEventConsumer.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/KafkaEventConsumer.java)
  Client event consumer
- [src/test/java/com/kafka/EntityKafkaEventConsumer.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/EntityKafkaEventConsumer.java)
  Generic entity event consumer
- [src/test/java/com/kafka/KafkaEventValidator.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/KafkaEventValidator.java)
- [src/test/java/com/kafka/EntityKafkaEventValidator.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/EntityKafkaEventValidator.java)
- [src/test/java/com/kafka/ScenarioContext.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/ScenarioContext.java)
  Per-scenario Kafka state

### Mock Backend

- [server/index.js](/Users/vivedesh/ai-kafka-validator/server/index.js)
  JWT auth, CRUD routes, business validations, dependency checks, Kafka publishing triggers
- [server/seed-data.json](/Users/vivedesh/ai-kafka-validator/server/seed-data.json)
  Immutable dataset
- [server/clients.json](/Users/vivedesh/ai-kafka-validator/server/clients.json)
  Runtime dataset used by json-server
- [server/resetData.js](/Users/vivedesh/ai-kafka-validator/server/resetData.js)
  Seed restore script

## Supported Execution Paths

1. Full regression: all feature files
2. Selective tagged runs via `-Dcucumber.filter.tags=...`
3. Failed-only rerun via `testng-rerun.xml`
4. Full Docker execution via [docker-compose.yml](/Users/vivedesh/ai-kafka-validator/docker-compose.yml)
5. Hybrid local execution via [docker-compose.kafka.yml](/Users/vivedesh/ai-kafka-validator/docker-compose.kafka.yml)

## Business Validation Summary

The backend currently enforces:

- account must reference an existing client
- portfolio must reference an existing client
- transaction must reference an existing client
- transaction must reference an existing account
- transaction client must match account owner
- client delete is blocked by dependent transactions, accounts, or portfolios
- account delete is blocked by dependent transactions

## Reporting Outputs

Execution writes reports to:

- `target/cucumber.json`
- `target/cucumber-report.html`
- `target/cucumber/cucumber-html-reports/overview-features.html`
- `target/rerun.txt`
- `target/surefire-reports`
