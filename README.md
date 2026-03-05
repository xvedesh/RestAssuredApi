# RestAssured API Sandbox

**Integrated JSON Server + Java (RestAssured + Cucumber) Test Framework**

## Overview
This repository provides a self-contained API testing ecosystem. It includes a mock backend and a modern Java-based test suite, designed for local development, containerized execution, and CI/CD integration.

* **Mock API Server:** Node.js, json-server, JWT-based authentication
* **Java Test Framework:** Java 11, Maven, RestAssured, Cucumber (JUnit 5)

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
mvn clean test -Dcucumber.filter.tags=@ClientData -DbaseURI=http://localhost:3000

```

**Linux/macOS (Maven Wrapper):**

```bash
./mvnw clean test -Dcucumber.filter.tags=@ClientData -DbaseURI=http://localhost:3000

```

**Windows PowerShell (Maven Wrapper):**

```bash
.\mvnw.cmd clean test --% -Dcucumber.filter.tags=@ClientData -DbaseURI=http://localhost:3000

```

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
      - name: Cleanup
        if: always()
        run: docker compose down

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

