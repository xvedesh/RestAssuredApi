# RestAssured API Sandbox

**Integrated JSON Server + Java (RestAssured + Cucumber) Test Framework**

## Overview
This repository provides a self-contained API testing ecosystem. It includes a mock backend and a modern Java-based test suite, designed for local development, containerized execution, and CI/CD integration.

* **Mock API Server:** Node.js, json-server, JWT-based authentication
* **Java Test Framework:** Java 11, Maven, RestAssured, Cucumber + TestNG

---

## Architecture

### 1. Mock API (json-server)
* **Authentication:** `POST /auth/login`
* **Protected Resources:** `/clients`, `/accounts`, `/transactions`, `/portfolios`
* **Data Persistence:** `server/clients.json`

### 2. Test Suite (api-tests)
* **Engine:** RestAssured for fluent API assertions
* **BDD:** Cucumber for Gherkin-style scenario management
* **Configuration:** Dynamic Base URI resolution via:
    * Java System Properties
    * Environment Variables
    * `config.properties`

---

## Repository Structure

```
+-- docker-compose.yml     # Multi-container orchestration
+-- config.properties      # Default framework configuration
+-- testng.xml             # Main TestNG suite (parallel execution)
+-- testng-rerun.xml       # TestNG suite for failed test rerun
+-- src/                   # Java test source code
+-- server/                # Node.js API server
|   +-- index.js           # Server logic & Auth middleware
|   +-- package.json       # Node dependencies
|   +-- clients.json       # Mock database
+-- Dockerfile             # Java test suite image definition
+-- server/Dockerfile      # API server image definition
```
---

## Setup & Prerequisites

| Environment | Requirements |
| --- | --- |
| **Local Mode** | Java 11, Node.js (Maven wrapper included in repo) |
| **Docker Mode** | Docker Desktop & Docker Compose |

---

## Quick Start

### Option A: Docker Mode (Recommended)

Build, start the server, run tests, and exit in one command.

**Run Everything:**

```bash
docker compose up --build --abort-on-container-exit --exit-code-from api-tests

```

**Run and Save Full Log (Windows PowerShell):**

```powershell
docker compose up --build --abort-on-container-exit --exit-code-from api-tests | Tee-Object run.log

```

**Run and Save Full Log (Linux/macOS):**

```bash
docker compose up --build --abort-on-container-exit --exit-code-from api-tests | tee run.log

```

After the run:
* `api-tests` exit code `0` means tests passed.
* Full console output is stored in `run.log`.
* Test artifacts are available on host under `./target` (mounted from container).

**Clean Up:**

```bash
docker compose down

```

### Docker Workflow for Remote Development

Use this when coding from another machine (for example via VS Code Remote SSH):
1. Open the repository on the remote machine.
2. Edit code normally.
3. Re-run Docker tests with one of the commands above.
4. Inspect `run.log` and `./target` after each run.
5. Repeat edit -> run -> inspect.

### Option B: Local Mode

Use this for active development and debugging.

**1. Start the API Server:**

```bash
cd server
npm install
npm start

```

*The server will run on: http://localhost:3000*

**2. Execute Tests (choose your environment):**

**IntelliJ IDEA (Bundled Maven):**

```bash
mvn clean test -Dtestng.suite.file=testng.xml -DbaseURI=http://localhost:3000

```

**Linux/macOS (Maven Wrapper):**

```bash
./mvnw clean test -Dtestng.suite.file=testng.xml -DbaseURI=http://localhost:3000

```

**Windows PowerShell (Maven Wrapper):**

```bash
.\mvnw.cmd clean test --% -Dtestng.suite.file=testng.xml -DbaseURI=http://localhost:3000

```

---

## Parallel Test Execution (TestNG)

Parallel execution is configured through TestNG suite files and Surefire properties.

### Main Parallel Run
* Suite file: `testng.xml`
* Current mode: `parallel="methods"` and `thread-count="4"`
* Command:

```bash
.\mvnw.cmd clean test --% -Dtestng.suite.file=testng.xml -DbaseURI=http://localhost:3000
```

### Failed Scenarios Rerun
* Failed scenarios list is generated to: `target/rerun.txt`
* Rerun suite file: `testng-rerun.xml`
* Command:

```bash
.\mvnw.cmd test --% -Dtestng.suite.file=testng-rerun.xml -DbaseURI=http://localhost:3000
```

### How to Change Parallelism
1. Update TestNG suite threads in `testng.xml`:
```xml
<suite name="Cucumber Main Suite" parallel="methods" thread-count="6">
```
2. Update Cucumber DataProvider thread count in `pom.xml` property:
```xml
<cucumber.threads>6</cucumber.threads>
```
3. Keep both values aligned for predictable CI behavior.

---

## CI/CD Integration

The following command is optimized for CI environments. It ensures a non-zero exit code if tests fail:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from api-tests

```

### GitHub Actions Example

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run API Tests
        run: docker compose up --build --abort-on-container-exit --exit-code-from api-tests
      - name: Rerun Failed Scenarios
        if: always()
        run: docker compose run --rm api-tests ./mvnw test -Dtestng.suite.file=testng-rerun.xml -DbaseURI=http://json-server:3000
      - name: Cleanup
        if: always()
        run: docker compose down

```

---

## Reports

After test execution, the framework generates:
* Cucumber JSON: `target/cucumber.json`
* Cucumber basic HTML: `target/cucumber-report.html`
* Cucumber PrettyReports: `target/cucumber/cucumber-html-reports/overview-features.html`
* TestNG/Surefire HTML dashboard: `target/surefire-reports/index.html`
* Failed scenarios list for rerun: `target/rerun.txt`

### Open Reports in Browser (Windows PowerShell)

```powershell
start .\target\cucumber\cucumber-html-reports\overview-features.html
start .\target\surefire-reports\index.html
```

If `start` does not work in your shell:

```powershell
Invoke-Item .\target\cucumber\cucumber-html-reports\overview-features.html
Invoke-Item .\target\surefire-reports\index.html
```

### Open Reports in Browser (Linux)

```bash
xdg-open ./target/cucumber/cucumber-html-reports/overview-features.html
xdg-open ./target/surefire-reports/index.html
```

### Open Reports in Browser (macOS)

```bash
open ./target/cucumber/cucumber-html-reports/overview-features.html
open ./target/surefire-reports/index.html
```

---

## Troubleshooting

### Connection Refused

* **Local Mode:** Ensure the server is running on port 3000.
* **Docker Mode:** Use the service name inside the Docker network: `http://json-server:3000`

### Cleaning Up Logs

To reduce console noise in RestAssured, use conditional logging:

```text
RestAssured.given()
    .log().ifValidationFails()

```

---

> **Note:** This project is intended for demonstration and testing purposes. It uses mock data and simplified authentication.

