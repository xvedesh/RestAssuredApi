# AI Kafka Validator - Setup / Onboarding Agent

The Setup / Onboarding Agent is a local RAG-style assistant for understanding, setting up, running, troubleshooting, and extending this framework.

## What It Does

It retrieves information from the repository itself, including:

- `README.md`
- `docs/*.md`
- `pom.xml`
- `config.properties`
- `docker-compose.yml`
- `docker-compose.kafka.yml`
- `Dockerfile` files
- feature files
- runners
- step definitions
- API classes
- Kafka utilities
- server files
- Failure Analysis Agent files

It is designed to answer questions such as:

- how to run the framework in Docker or locally
- how Kafka validation works in this project
- which files implement relationship validation
- how to run tagged suites such as negative or transaction Kafka scenarios
- why Kafka tests are not working locally
- how the framework could be adapted for real Kafka clusters, CI/CD, databases, or real backends

## Retrieval Approach

Version 1 uses deterministic local retrieval:

- project files are indexed from the repository itself
- markdown files are chunked by section
- feature files are chunked by feature and scenario boundaries
- code and config files are chunked in small line windows
- the agent scores chunks lexically against the user question and answers from the highest-scoring sources first

This keeps the agent local, reproducible, and easy to explain. It does not require external AI APIs or a remote vector database.

## Entry Points

Run one question directly:

```bash
sh ./mvnw -q exec:java -Dexec.mainClass=com.analysis.onboarding.SetupOnboardingAgent -Dagent.question="How do I run only negative scenarios?"
```

Run with command-line arguments:

```bash
sh ./mvnw -q exec:java -Dexec.mainClass=com.analysis.onboarding.SetupOnboardingAgent -Dexec.args="How does Kafka publishing work in this framework?"
```

Run interactive mode:

```bash
sh ./mvnw -q exec:java -Dexec.mainClass=com.analysis.onboarding.SetupOnboardingAgent
```

## Output Style

Depending on the question, the agent answers with structured sections such as:

- direct answer
- exact steps / commands
- relevant project files
- environment checks
- common mistakes to avoid
- optional next step

For enterprise adaptation questions it separates:

- current framework behavior
- what would change in a real system
- recommended integration pattern
- constraints / assumptions
- safe next steps

## Environment Checks

For setup and troubleshooting questions, the agent performs lightweight checks such as:

- Java runtime version
- required file presence
- Docker CLI availability
- Docker daemon responsiveness
- Node.js and npm availability
- Kafka port reachability on `localhost:9092`
- mock API health endpoint reachability on `http://localhost:3000/health`

## Relationship to the Failure Analysis Agent

- the Setup / Onboarding Agent helps users understand, set up, run, troubleshoot, and extend the framework
- the Failure Analysis Agent explains why a failed run happened after execution artifacts are produced

Both agents are local and deterministic. Neither depends on external AI APIs or a remote vector database.
