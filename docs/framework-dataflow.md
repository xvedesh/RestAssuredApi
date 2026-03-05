# Framework Data Flow / Execution Map

## 1) High-Level Start -> Finish Diagram

```text
Command (mvn test OR docker compose up)
  -> pom.xml (Surefire reads ${testng.suite.file})
  -> testng.xml / testng-rerun.xml
  -> com.runners.CukesRunner or com.runners.FailedTestRunner
  -> AbstractTestNGCucumberTests.scenarios() DataProvider
  -> Cucumber loads feature(s) and glue (com/step_defs)
  -> Step methods in ClientDataStepDefs
  -> APIMap.getApiValidator(block) -> concrete API class
  -> BaseTest + ClientAPI perform HTTP calls + validations
  -> Hooks.tearDown() clears thread-local state
  -> Cucumber/TestNG/Surefire write reports to target/
```

## 2) Numbered Step-by-Step Flow (Start to End)

1. File: `pom.xml`  
   Class: Maven Surefire config  
   Method/Section: `<plugin>maven-surefire-plugin</plugin>`, `<suiteXmlFiles>`, `${testng.suite.file}`  
   What it does: Surefire starts TestNG using suite XML specified by `-Dtestng.suite.file` (default `testng.xml`).

2. File: `testng.xml`  
   Class: TestNG suite definition  
   Method/Section: `<class name="com.runners.CukesRunner"/>`  
   What it does: Main execution path points TestNG to `CukesRunner` and enables method-level parallelism (`thread-count="4"`).

3. File: `testng-rerun.xml`  
   Class: TestNG suite definition  
   Method/Section: `<class name="com.runners.FailedTestRunner"/>`  
   What it does: Rerun execution path points TestNG to `FailedTestRunner` with single-thread mode.

4. File: `src/test/java/com/runners/CukesRunner.java`  
   Class: `com.runners.CukesRunner`  
   Method(s): `scenarios()`; `@CucumberOptions(...)`  
   What it does: Extends `AbstractTestNGCucumberTests`, provides parallel scenario DataProvider, and configures features/glue/plugins/tags.

5. File: `src/test/java/com/runners/FailedTestRunner.java`  
   Class: `com.runners.FailedTestRunner`  
   Method(s): `scenarios()`; `@CucumberOptions(features = "@target/rerun.txt", ...)`  
   What it does: Runs only failed scenarios listed in `target/rerun.txt`.

6. File: `src/test/resources/features/ClientService.feature`  
   Class: Gherkin feature file  
   Method/Section: Scenario Outline steps + `Examples` table  
   What it does: Defines the scenario flow and passes `block` values (`client`, `account`, `portfolio`, `transaction`).

7. File: `src/test/java/com/step_defs/ClientDataStepDefs.java`  
   Class: `com.step_defs.ClientDataStepDefs`  
   Method(s):  
   - `i_send_post_request_to_get_authorization_token()`  
   - `i_send_post_request_to_create_new_data(String)`  
   - `i_send_get_request_to_data_service_and_validate_created_data(String)`  
   - `i_send_put_request_to_update_crated_data_and_validate_updated_data(String)`  
   - `i_send_patch_request_to_update_created_data_and_validate_updated_data(String)`  
   - `i_send_delete_request_to_delete_created_data_validate_data_removal(String)`  
   What it does: Binds Gherkin steps to Java methods and orchestrates auth + CRUD + validations through API abstractions.

8. File: `src/test/java/com/api/BaseTest.java`  
   Class: `com.api.BaseTest`  
   Method(s): `generateToken()`, `returnCredentials()`, `returnAuthHeaders()`  
   What it does: Builds credentials, calls auth endpoint, stores bearer token in thread-local context, and returns headers for subsequent requests.

9. File: `src/test/java/com/api/APIMap.java`  
   Class: `com.api.APIMap`  
   Method(s): `getApiValidator(String)`  
   What it does: Resolves the `block` string to a corresponding `PayLoadValidator` implementation (`ClientAPI`, `AccountAPI`, `PortfolioAPI`, `TransactionAPI`).

10. File: `src/test/java/com/interfaces/PayLoadValidator.java`  
    Class: `com.interfaces.PayLoadValidator`  
    Method(s): `post()`, `patch()`, `put()`, `delete()`, `get()`, `validatePayload(...)`, `validateDeletion()`  
    What it does: Defines the contract step definitions use regardless of specific API resource.

11. File: `src/test/java/com/api/ClientAPI.java`  
    Class: `com.api.ClientAPI`  
    Method(s): `post()`, `get()`, `put()`, `patch()`, `delete()`, `validatePayload(...)`, `validateDeletion()`  
    What it does: Executes real REST calls for client resource and validates response payload/deletion status.

12. File: `src/test/java/com/pojo/Client.java`  
    Class: `com.pojo.Client` and nested `Client.Address`  
    Method(s): setters/getters, `generatePojoMap()`  
    What it does: Holds expected client state and converts it into map form used in payload assertions.

13. File: `src/test/java/com/api/ClientAPI.java`  
    Class: `com.api.ClientAPI`  
    Method(s): `returnBody()`, `putBody()`, `patchBody()`, `getAttributeList()`  
    What it does: Generates test data with Faker and keeps expected values in thread-local `Client`/`Address`.

14. File: `src/test/java/com/api/AccountAPI.java`  
    Class: `com.api.AccountAPI`  
    Method(s): all `PayLoadValidator` methods (currently empty / `get()` returns `null`)  
    What it does: Placeholder implementation for account block; currently no real API logic.

15. File: `src/test/java/com/api/PortfolioAPI.java`  
    Class: `com.api.PortfolioAPI`  
    Method(s): all `PayLoadValidator` methods (currently empty / `get()` returns `null`)  
    What it does: Placeholder implementation for portfolio block; currently no real API logic.

16. File: `src/test/java/com/api/TransactionAPI.java`  
    Class: `com.api.TransactionAPI`  
    Method(s): all `PayLoadValidator` methods (currently empty / `get()` returns `null`)  
    What it does: Placeholder implementation for transaction block; currently no real API logic.

17. File: `src/test/java/com/step_defs/Hooks.java`  
    Class: `com.step_defs.Hooks`  
    Method(s): `tearDown()` with `@After`  
    What it does: Clears thread-local test context after each scenario via `ClientAPI.clearThreadContext()` and `BaseTest.clearThreadContext()`.

18. File: `src/test/java/com/utils/ConfigurationReader.java`  
    Class: `com.utils.ConfigurationReader`  
    Method(s): static initializer, `getProperty(String)`  
    What it does: Loads `config.properties` once and provides key-value settings to framework classes.

19. File: `config.properties`  
    Class: runtime config file  
    Method/Section: `baseURI`, credentials, endpoint keys  
    What it does: Supplies default environment URL, credentials, and endpoint paths.

20. File: `src/test/java/com/runners/CukesRunner.java`  
    Class: `com.runners.CukesRunner`  
    Method/Section: plugin list  
    What it does: Writes run artifacts (`target/cucumber.json`, `target/cucumber-report.html`, `target/rerun.txt`, `target/cucumber/...` PrettyReports).

21. File: `target/surefire-reports/*` (generated)  
    Class: Surefire/TestNG report artifacts  
    Method/Section: `index.html`, `testng-results.xml`, etc.  
    What it does: Captures TestNG suite/test result summary after execution ends.

## 3) Key Components

### Runner(s)
- Main: `src/test/java/com/runners/CukesRunner.java` (`AbstractTestNGCucumberTests`, parallel data provider).
- Rerun: `src/test/java/com/runners/FailedTestRunner.java` (reads `@target/rerun.txt`).

### TestNG suite config and runner mapping
- `testng.xml` maps to `com.runners.CukesRunner`.
- `testng-rerun.xml` maps to `com.runners.FailedTestRunner`.
- `pom.xml` Surefire points to suite file via `${testng.suite.file}`.

### Cucumber hooks
- `src/test/java/com/step_defs/Hooks.java` has only `@After` hook.
- It performs cleanup of thread-local API/auth state; no `@Before` hook exists in current repo.

### Step definition package and bindings
- Package: `src/test/java/com/step_defs`.
- Step annotations in `ClientDataStepDefs` (`@Given`, `@Then`) bind to `ClientService.feature` steps.
- Glue path configured in runners: `glue = "com/step_defs"`.

### Config loading (env/url/credentials)
- `BaseTest.baseURI` selection order in `BaseTest`:  
  `System.getProperty("baseURI")` -> `System.getenv("BASE_URI")` -> `ConfigurationReader.getProperty("baseURI")`.
- Credentials and endpoint paths come from `config.properties` via `ConfigurationReader`.

### Data layer (fixtures/builders/providers)
- `ClientAPI` uses Faker to build randomized client payloads (`returnBody`, `putBody`, `patchBody`).
- `Client` POJO stores expected data; `generatePojoMap()` builds expected assertion map.
- Scenario data dimension is the `Examples` block values in `ClientService.feature`.

### Service layer
- Contract: `PayLoadValidator`.
- Dispatcher: `APIMap.getApiValidator(block)`.
- Implementations: `ClientAPI` (active), `AccountAPI`/`PortfolioAPI`/`TransactionAPI` (placeholders).

### Utilities
- `ConfigurationReader`: property-file loading.
- `BaseTest`: auth token generation and request header utility.
- No custom TestNG listeners/retry/wait helpers are present in current repo.

### Reporting
- Cucumber plugins configured in `CukesRunner` write:
  - `target/cucumber.json`
  - `target/cucumber-report.html`
  - `target/rerun.txt`
  - `target/cucumber` (PrettyReports output)
- Surefire/TestNG writes additional reports in `target/surefire-reports`.

## 4) Call Chain(s)

### Main scenario execution chain
`Surefire(pom.xml) -> testng.xml -> CukesRunner.scenarios() -> Cucumber Scenario -> ClientDataStepDefs.i_send_post_request_to_get_authorization_token() -> BaseTest.generateToken() -> BaseTest.returnCredentials()/returnAuthHeaders() -> auth endpoint`

### Resource operation chain (client block)
`ClientDataStepDefs.i_send_post_request_to_create_new_data("client") -> APIMap.getApiValidator("client") -> new ClientAPI().post() -> ClientAPI.returnBody() -> HTTP POST /clients`

### Validation chain
`ClientDataStepDefs.i_send_get_request_to_data_service_and_validate_created_data("client") -> APIMap.getApiValidator("client").get() -> ClientAPI.get() -> APIMap.getApiValidator("client").validatePayload(payload) -> Client.generatePojoMap() + ClientAPI.generateResponseMap() -> TestNG Assert.assertEquals(...)`

### End-of-scenario cleanup chain
`Cucumber @After -> Hooks.tearDown() -> ClientAPI.clearThreadContext() + BaseTest.clearThreadContext()`

### Rerun chain
`Surefire(pom.xml, -Dtestng.suite.file=testng-rerun.xml) -> testng-rerun.xml -> FailedTestRunner -> CucumberOptions(features="@target/rerun.txt")`

## 5) Where To Change What

### Change base URL / environment selection
- Primary logic: `src/test/java/com/api/BaseTest.java` (`baseURI` field initialization).
- Default values: `config.properties`.
- Runtime overrides:
  - CLI: `-DbaseURI=...`
  - Env: `BASE_URI=...` (used in Docker Compose for `api-tests` service).

### Add a new feature/test
1. Add/extend Gherkin in `src/test/resources/features/*.feature`.
2. Add matching step methods in `src/test/java/com/step_defs/*StepDefs.java`.
3. If new resource type is needed:
   - implement `PayLoadValidator` class under `src/test/java/com/api/`
   - register it in `APIMap` static supplier map.
4. Ensure tag strategy matches runner `@CucumberOptions(tags = ...)` if needed.

### Change authentication logic
- `src/test/java/com/api/BaseTest.java`:
  - `generateToken()` (auth call behavior)
  - `returnCredentials()` (username/password source)
  - `returnAuthHeaders()` (header composition).
- Credentials and auth endpoint keys are in `config.properties`.

### Run a subset (tags/groups)
- Current tag filter for main run is hardcoded in `CukesRunner`:
  - `@CucumberOptions(tags = "@ClientData")`.
- To run only failed scenarios:
  - use `testng-rerun.xml` + `FailedTestRunner` reading `@target/rerun.txt`.
- Suite selection is done via Maven property:
  - `-Dtestng.suite.file=testng.xml` or `-Dtestng.suite.file=testng-rerun.xml`.

## Multiple Execution Paths Present

1. **Main full run path**: `testng.xml -> CukesRunner -> feature files by tag`.
2. **Failed-only rerun path**: `testng-rerun.xml -> FailedTestRunner -> @target/rerun.txt`.
3. **Execution environments**:
   - Local Maven (`mvn test`)
   - Docker (`docker compose up ...`) using:
     - `Dockerfile` for test container (`CMD ["mvn","-q","test"]`)
     - `docker-compose.yml` sets `BASE_URI=http://json-server:3000`.
