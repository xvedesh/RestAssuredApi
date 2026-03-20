# AI Kafka Validator

AI Kafka Validator is an AI-powered Kafka messaging validation testing framework designed to validate both REST APIs and asynchronous event-driven flows. It combines RestAssured, Cucumber, TestNG, JSON Server, and Kafka to verify CRUD behavior, event publishing, relationship rules, negative business validations, and cross-entity workflows in a single framework that is easy to run locally or in Docker.

## Selling Executive Introduction

This framework demonstrates how a Senior SDET can validate more than HTTP status codes. It proves that:

- APIs return the correct business response
- the backend publishes the correct Kafka event
- invalid business relationships are rejected consistently
- blocked operations do not emit unwanted events
- runtime test data can be restored safely through a repeatable seed/reset mechanism

The result is a compact but realistic quality engineering platform for event-aware API systems.

## Key Capabilities

- RestAssured-based API automation with Cucumber + TestNG execution
- full CRUD coverage for `Client`, `Account`, `Portfolio`, and `Transaction`
- Kafka E2E validation for successful create, update, patch, and delete flows
- negative business validation scenarios with “no Kafka event published” assertions
- cross-entity business flow coverage across client, account, and transaction
- JWT-based authentication flow against the mock API
- Docker-based execution for Kafka, mock API, and Java test suite
- local execution support with Kafka in Docker and server/tests on host
- protected runtime data via `seed-data.json -> clients.json` reset flow
- HTML, JSON, Pretty Cucumber, and Surefire reports

## Architecture Overview

The framework has three active runtime layers:

- `json-server`
  Purpose: mock API backend with JWT auth, CRUD routes, business validation rules, and Kafka publishing
- `kafka`
  Purpose: single-broker event transport for API lifecycle messages
- `api-tests`
  Purpose: Java test suite that drives API calls, consumes Kafka messages, and validates API-to-event consistency

Execution flow:

1. Cucumber step triggers an API action through a Java API class
2. Request hits the Node mock service
3. Service validates business rules and relationships
4. Service persists changes into `clients.json`
5. Service publishes a Kafka event when the operation succeeds
6. Java Kafka consumer reads the matching message by business key
7. Test validates HTTP response, Kafka metadata, and Kafka payload

## Domain Coverage

Supported entities:

- `Client`
- `Account`
- `Portfolio`
- `Transaction`

API coverage:

- `POST /clients`, `GET /clients/{id}`, `PUT /clients/{id}`, `PATCH /clients/{id}`, `DELETE /clients/{id}`
- `POST /accounts`, `GET /accounts/{id}`, `PUT /accounts/{id}`, `PATCH /accounts/{id}`, `DELETE /accounts/{id}`
- `POST /portfolios`, `GET /portfolios/{id}`, `PUT /portfolios/{id}`, `PATCH /portfolios/{id}`, `DELETE /portfolios/{id}`
- `POST /transactions`, `GET /transactions/{id}`, `PUT /transactions/{id}`, `PATCH /transactions/{id}`, `DELETE /transactions/{id}`

Kafka topics:

- `client-events`
- `account-events`
- `portfolio-events`
- `transaction-events`

Kafka event coverage currently implemented:

- Client: `CLIENT_CREATED`, `CLIENT_UPDATED`, `CLIENT_PATCHED`, `CLIENT_DELETED`
- Account: `ACCOUNT_CREATED`, `ACCOUNT_UPDATED`, `ACCOUNT_PATCHED`, `ACCOUNT_DELETED`
- Portfolio: `PORTFOLIO_CREATED`, `PORTFOLIO_UPDATED`, `PORTFOLIO_PATCHED`, `PORTFOLIO_DELETED`
- Transaction: `TRANSACTION_CREATED`, `TRANSACTION_UPDATED`, `TRANSACTION_PATCHED`, `TRANSACTION_DELETED`

Current executable feature set in the project:

- happy-path CRUD validation through [ClientService.feature](/Users/vivedesh/ai-kafka-validator/src/test/resources/features/ClientService.feature)
- Kafka E2E validation through:
  - [ClientKafkaE2E.feature](/Users/vivedesh/ai-kafka-validator/src/test/resources/features/ClientKafkaE2E.feature)
  - [AccountKafkaE2E.feature](/Users/vivedesh/ai-kafka-validator/src/test/resources/features/AccountKafkaE2E.feature)
  - [PortfolioKafkaE2E.feature](/Users/vivedesh/ai-kafka-validator/src/test/resources/features/PortfolioKafkaE2E.feature)
  - [TransactionKafkaE2E.feature](/Users/vivedesh/ai-kafka-validator/src/test/resources/features/TransactionKafkaE2E.feature)
- negative validation coverage through [NegativeValidationE2E.feature](/Users/vivedesh/ai-kafka-validator/src/test/resources/features/NegativeValidationE2E.feature)
- chained business flow coverage through [CrossEntityBusinessFlow.feature](/Users/vivedesh/ai-kafka-validator/src/test/resources/features/CrossEntityBusinessFlow.feature)

## Business Rules / Relationships

The framework already enforces business validation rules inside the Node mock backend.

Relationship rules:

- `Account.clientId` must reference an existing client
- `Portfolio.clientId` must reference an existing client
- `Transaction.clientId` must reference an existing client
- `Transaction.accountId` must reference an existing account
- `Transaction.clientId` must match the owner of `Transaction.accountId`

Delete dependency rules:

- Client delete is blocked if dependent transactions exist
- Client delete is blocked if dependent accounts exist
- Client delete is blocked if dependent portfolios exist
- Account delete is blocked if dependent transactions exist
- Portfolio delete is allowed without dependency checks
- Transaction delete is allowed without dependency checks

HTTP behavior used in the current implementation:

- `400 Bad Request` for relationship and business validation failures
- `409 Conflict` for dependency-blocked delete operations
- `404 Not Found` when the target resource itself does not exist

Example server-side messages returned by the current code:

- `Client not found for clientId=...`
- `Account not found for accountId=...`
- `Transaction clientId does not match account owner`
- `Cannot delete client with existing accounts`
- `Cannot delete client with existing portfolios`
- `Cannot delete client with existing transactions`
- `Cannot delete account with existing transactions`

## Kafka Event Validation

Kafka validation is not based on “first message in topic”. The framework consumes by matching the business key for the current scenario.

Current matching strategy:

- Client events are matched by `clientId` and `eventType`
- Account, Portfolio, and Transaction events are matched by `entityId` and `eventType`

Current event shape:

Client events:

```json
{
  "eventType": "CLIENT_CREATED",
  "entityType": "CLIENT",
  "clientId": "....",
  "timestamp": "...",
  "payload": { }
}
```

Entity events:

```json
{
  "eventType": "ACCOUNT_CREATED",
  "entityType": "ACCOUNT",
  "entityId": "A123456",
  "timestamp": "...",
  "payload": { }
}
```

What the framework validates for successful Kafka scenarios:

- expected topic
- expected Kafka key
- expected event type
- expected entity type
- payload equality against the latest API response

What the framework validates for negative Kafka scenarios:

- failed operations must not publish the matching Kafka event within the configured timeout

Relevant Kafka support code:

- [server/kafkaPublisher.js](/Users/vivedesh/ai-kafka-validator/server/kafkaPublisher.js)
- [src/test/java/com/kafka/KafkaEventConsumer.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/KafkaEventConsumer.java)
- [src/test/java/com/kafka/EntityKafkaEventConsumer.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/EntityKafkaEventConsumer.java)
- [src/test/java/com/kafka/KafkaEventValidator.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/KafkaEventValidator.java)
- [src/test/java/com/kafka/EntityKafkaEventValidator.java](/Users/vivedesh/ai-kafka-validator/src/test/java/com/kafka/EntityKafkaEventValidator.java)

## Project Structure

```text
.
|-- README.md
|-- pom.xml
|-- config.properties
|-- docker-compose.yml
|-- docker-compose.kafka.yml
|-- testng.xml
|-- testng-rerun.xml
|-- src/test/java/com
|   |-- api
|   |-- interfaces
|   |-- kafka
|   |-- pojo
|   |-- runners
|   |-- step_defs
|   `-- utils
|-- src/test/resources/features
|   |-- ClientService.feature
|   |-- ClientKafkaE2E.feature
|   |-- AccountKafkaE2E.feature
|   |-- PortfolioKafkaE2E.feature
|   |-- TransactionKafkaE2E.feature
|   |-- NegativeValidationE2E.feature
|   `-- CrossEntityBusinessFlow.feature
`-- server
    |-- index.js
    |-- kafkaPublisher.js
    |-- resetData.js
    |-- seed-data.json
    |-- clients.json
    |-- package.json
    `-- Dockerfile
```

## Prerequisites

Common:

- Docker Desktop
- Docker Compose

Local execution:

- Java 17
- Node.js 20+

## Configuration

Default configuration lives in [config.properties](/Users/vivedesh/ai-kafka-validator/config.properties):

```properties
baseURI=http://localhost:3000
kafkaBootstrapServers=localhost:9092
kafkaClientEventsTopic=client-events
kafkaAccountEventsTopic=account-events
kafkaPortfolioEventsTopic=portfolio-events
kafkaTransactionEventsTopic=transaction-events
kafkaConsumerTimeoutMs=10000
kafkaPollIntervalMs=500
kafkaConsumerGroupPrefix=ai-kafka-validator-tests
username=user1
password=password1
authEndPoint=/auth/login
allClientsEndPoint=/clients
clientEndPoint=/clients/{clientId}
allAccountsEndPoint=/accounts
accountEndPoint=/accounts/{clientId}
allTransactionsEndPoint=/transactions
transactionEndPoint=/transactions/{clientId}
allPortfoliosEndPoint=/portfolios
portfolioEndPoint=/portfolios/{clientId}
```

Resolution order in the Java test layer:

1. JVM system properties
2. environment variables
3. `config.properties`

Key runtime environment variables used by the server and Docker:

- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_CLIENT_EVENTS_TOPIC`
- `KAFKA_ACCOUNT_EVENTS_TOPIC`
- `KAFKA_PORTFOLIO_EVENTS_TOPIC`
- `KAFKA_TRANSACTION_EVENTS_TOPIC`
- `BASE_URI`

## Test Data / Seed Reset

The framework protects mock data through a seed/reset mechanism.

Current data files:

- [server/seed-data.json](/Users/vivedesh/ai-kafka-validator/server/seed-data.json)
  Purpose: immutable source dataset
- [server/clients.json](/Users/vivedesh/ai-kafka-validator/server/clients.json)
  Purpose: runtime file used by `json-server`

Current reset behavior:

- `npm run reset-data` copies `seed-data.json` into `clients.json`
- `npm start` automatically executes `npm run reset-data && node index.js`
- Docker uses the same `npm start`, so runtime data is restored there too

Manual reset command:

```bash
cd server
npm run reset-data
```

This allows repeated test execution without permanently damaging the source dataset.

## How to Run

### Option 1: Full Docker Execution

Run the full stack:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from api-tests
```

Save the run log:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from api-tests | tee run.log
```

Stop and clean containers:

```bash
docker compose down -v
```

### Option 2: Hybrid Local Execution

Start Kafka only:

```bash
docker compose -f docker-compose.kafka.yml up -d
```

Start the mock API:

```bash
cd server
npm install
KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
KAFKA_CLIENT_EVENTS_TOPIC=client-events \
KAFKA_ACCOUNT_EVENTS_TOPIC=account-events \
KAFKA_PORTFOLIO_EVENTS_TOPIC=portfolio-events \
KAFKA_TRANSACTION_EVENTS_TOPIC=transaction-events \
npm start
```

Run the full Java suite:

```bash
cd /Users/vivedesh/ai-kafka-validator
sh ./mvnw clean test \
  -Dtestng.suite.file=testng.xml \
  -DbaseURI=http://localhost:3000 \
  -DkafkaBootstrapServers=localhost:9092 \
  -DkafkaClientEventsTopic=client-events \
  -DkafkaAccountEventsTopic=account-events \
  -DkafkaPortfolioEventsTopic=portfolio-events \
  -DkafkaTransactionEventsTopic=transaction-events
```

Run the Failure Analysis Agent after a failed run:

```bash
sh ./mvnw -q exec:java -Dexec.mainClass=com.analysis.failure.FailureAnalysisAgent
```

Stop Kafka:

```bash
docker compose -f docker-compose.kafka.yml down -v
```

### Option 3: Windows PowerShell

Start Kafka:

```powershell
docker compose -f docker-compose.kafka.yml up -d
```

Start the mock API:

```powershell
cd server
npm install
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:KAFKA_CLIENT_EVENTS_TOPIC="client-events"
$env:KAFKA_ACCOUNT_EVENTS_TOPIC="account-events"
$env:KAFKA_PORTFOLIO_EVENTS_TOPIC="portfolio-events"
$env:KAFKA_TRANSACTION_EVENTS_TOPIC="transaction-events"
npm start
```

Run the suite:

```powershell
.\mvnw.cmd clean test --% -Dtestng.suite.file=testng.xml -DbaseURI=http://localhost:3000 -DkafkaBootstrapServers=localhost:9092 -DkafkaClientEventsTopic=client-events -DkafkaAccountEventsTopic=account-events -DkafkaPortfolioEventsTopic=portfolio-events -DkafkaTransactionEventsTopic=transaction-events
```

Run the Failure Analysis Agent:

```powershell
.\mvnw.cmd -q exec:java --% -Dexec.mainClass=com.analysis.failure.FailureAnalysisAgent
```

### Readiness Checks

Health endpoint:

```bash
curl http://localhost:3000/health
```

Auth smoke check:

```bash
curl -X POST http://localhost:3000/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user1","password":"password1"}'
```

## Tags and Execution Strategy

Current tag groups in the project:

- `@ClientData`
  Purpose: CRUD regression outline for client, account, portfolio, transaction
- `@KafkaClientData`
  Purpose: all client Kafka E2E scenarios
- `@KafkaAccountData`
  Purpose: all account Kafka E2E scenarios
- `@KafkaPortfolioData`
  Purpose: all portfolio Kafka E2E scenarios
- `@KafkaTransactionData`
  Purpose: all transaction Kafka E2E scenarios
- `@Negative`
  Purpose: negative business validation suite
- `@BusinessFlow`
  Purpose: business-flow scenarios
- `@CrossEntityE2E`
  Purpose: chained client-account-transaction flow

Entity-level Kafka tags:

- Client:
  `@KafkaCreateClient`, `@KafkaUpdateClient`, `@KafkaPatchClient`, `@KafkaDeleteClient`
- Account:
  `@KafkaCreateAccount`, `@KafkaUpdateAccount`, `@KafkaDeleteAccount`
- Portfolio:
  `@KafkaCreatePortfolio`, `@KafkaPatchPortfolio`, `@KafkaDeletePortfolio`
- Transaction:
  `@KafkaCreateTransaction`, `@KafkaUpdateTransaction`, `@KafkaDeleteTransaction`

Run only happy-path CRUD regression:

```bash
sh ./mvnw clean test \
  -Dtestng.suite.file=testng.xml \
  -Dcucumber.filter.tags="@ClientData" \
  -DbaseURI=http://localhost:3000 \
  -DkafkaBootstrapServers=localhost:9092 \
  -DkafkaClientEventsTopic=client-events \
  -DkafkaAccountEventsTopic=account-events \
  -DkafkaPortfolioEventsTopic=portfolio-events \
  -DkafkaTransactionEventsTopic=transaction-events
```

Run only Kafka scenarios:

```bash
sh ./mvnw clean test \
  -Dtestng.suite.file=testng.xml \
  -Dcucumber.filter.tags="@KafkaClientData or @KafkaAccountData or @KafkaPortfolioData or @KafkaTransactionData" \
  -DbaseURI=http://localhost:3000 \
  -DkafkaBootstrapServers=localhost:9092 \
  -DkafkaClientEventsTopic=client-events \
  -DkafkaAccountEventsTopic=account-events \
  -DkafkaPortfolioEventsTopic=portfolio-events \
  -DkafkaTransactionEventsTopic=transaction-events
```

Run negative validation:

```bash
sh ./mvnw clean test \
  -Dtestng.suite.file=testng.xml \
  -Dcucumber.filter.tags="@Negative" \
  -DbaseURI=http://localhost:3000 \
  -DkafkaBootstrapServers=localhost:9092 \
  -DkafkaClientEventsTopic=client-events \
  -DkafkaAccountEventsTopic=account-events \
  -DkafkaPortfolioEventsTopic=portfolio-events \
  -DkafkaTransactionEventsTopic=transaction-events
```

Run chained business flow:

```bash
sh ./mvnw clean test \
  -Dtestng.suite.file=testng.xml \
  -Dcucumber.filter.tags="@CrossEntityE2E" \
  -DbaseURI=http://localhost:3000 \
  -DkafkaBootstrapServers=localhost:9092 \
  -DkafkaClientEventsTopic=client-events \
  -DkafkaAccountEventsTopic=account-events \
  -DkafkaPortfolioEventsTopic=portfolio-events \
  -DkafkaTransactionEventsTopic=transaction-events
```

Run one exact Kafka scenario:

```bash
sh ./mvnw clean test \
  -Dtestng.suite.file=testng.xml \
  -Dcucumber.filter.tags="@KafkaDeleteClient" \
  -DbaseURI=http://localhost:3000 \
  -DkafkaBootstrapServers=localhost:9092 \
  -DkafkaClientEventsTopic=client-events \
  -DkafkaAccountEventsTopic=account-events \
  -DkafkaPortfolioEventsTopic=portfolio-events \
  -DkafkaTransactionEventsTopic=transaction-events
```

Current runner behavior:

- without `-Dcucumber.filter.tags=...`, the runner executes all features
- with `-Dcucumber.filter.tags=...`, execution is filtered at runtime

## Reporting

Generated report artifacts:

- Cucumber HTML:
  [target/cucumber-report.html](/Users/vivedesh/ai-kafka-validator/target/cucumber-report.html)
- Pretty Cucumber report:
  [target/cucumber/cucumber-html-reports/overview-features.html](/Users/vivedesh/ai-kafka-validator/target/cucumber/cucumber-html-reports/overview-features.html)
- Cucumber JSON:
  [target/cucumber.json](/Users/vivedesh/ai-kafka-validator/target/cucumber.json)
- Surefire/TestNG report:
  [target/surefire-reports/index.html](/Users/vivedesh/ai-kafka-validator/target/surefire-reports/index.html)
- Failed scenario rerun list:
  [target/rerun.txt](/Users/vivedesh/ai-kafka-validator/target/rerun.txt)
- Automatic failure analysis markdown:
  [target/failure-analysis.md](/Users/vivedesh/ai-kafka-validator/target/failure-analysis.md)
- Automatic failure analysis HTML:
  [target/surefire-reports/failure-analysis.html](/Users/vivedesh/ai-kafka-validator/target/surefire-reports/failure-analysis.html)

Open reports on macOS:

```bash
open target/cucumber-report.html
open target/cucumber/cucumber-html-reports/overview-features.html
open target/surefire-reports/index.html
open target/surefire-reports/failure-analysis.html
```

Open reports on Windows PowerShell:

```powershell
start target\cucumber-report.html
start target\cucumber\cucumber-html-reports\overview-features.html
start target\surefire-reports\index.html
start target\surefire-reports\failure-analysis.html
```

Rerun failed scenarios:

```bash
sh ./mvnw test \
  -Dtestng.suite.file=testng-rerun.xml \
  -DbaseURI=http://localhost:3000 \
  -DkafkaBootstrapServers=localhost:9092 \
  -DkafkaClientEventsTopic=client-events \
  -DkafkaAccountEventsTopic=account-events \
  -DkafkaPortfolioEventsTopic=portfolio-events \
  -DkafkaTransactionEventsTopic=transaction-events
```

Run the Failure Analysis Agent for only the most recent failed scenario:

```bash
sh ./mvnw -q exec:java \
  -Dexec.mainClass=com.analysis.failure.FailureAnalysisAgent \
  -Dfailure.analysis.mode=latest
```

Generated analysis artifact:

- Markdown failure report:
  [target/failure-analysis.md](/Users/vivedesh/ai-kafka-validator/target/failure-analysis.md)

Automatic trigger behavior:

- after TestNG execution finishes, the framework automatically runs the Failure Analysis Agent
- if failed scenarios are present, it writes:
  - `target/failure-analysis.md`
  - `target/failure-analysis.html`
  - `target/surefire-reports/failure-analysis.html`
  - `target/cucumber/cucumber-html-reports/failure-analysis.html`

## Enterprise Adoption / How to Extend

The framework is intentionally small, but the extension path is already clear and practical.

Safe extension points already present:

- add a new API domain by creating:
  - one POJO
  - one `*API.java`
  - one feature file
  - Kafka assertions through the existing entity Kafka support
- add new business rules in the Node server before persistence
- add new negative validation scenarios without refactoring the framework core
- add more chained E2E flows across domains
- scale selective execution using existing tag groups

Recommended extension path:

1. add the new entity to `server/seed-data.json`
2. expose routes in `server/index.js`
3. add the API class and POJO
4. add feature coverage
5. add Kafka topic and event validation only if the entity emits events

## Why This Framework is Valuable

This project is valuable because it shows more than CRUD automation.

It demonstrates:

- API automation with maintainable test layering
- event-driven validation beyond synchronous REST assertions
- business relationship enforcement at the mock-service level
- negative coverage that proves invalid requests do not leak Kafka side effects
- chained domain flow validation across related entities
- practical data protection through seed/reset instead of brittle manual cleanup
- runnable local and Docker execution for portfolio, demo, and CI use cases

In short, AI Kafka Validator represents a realistic Senior SDET-style approach to validating API systems that publish business events and must preserve data integrity across connected domains.
