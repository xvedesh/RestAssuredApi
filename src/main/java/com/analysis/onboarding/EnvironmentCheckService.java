package com.analysis.onboarding;

import com.analysis.onboarding.model.EnvironmentCheck;
import com.analysis.onboarding.model.EnvironmentReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EnvironmentCheckService {
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(3);

    public EnvironmentReport runBasicChecks(Path projectRoot) {
        List<EnvironmentCheck> checks = new ArrayList<>();
        checks.add(new EnvironmentCheck("Java runtime", "INFO", System.getProperty("java.version")));
        checks.add(fileCheck(projectRoot.resolve("pom.xml"), "pom.xml present"));
        checks.add(fileCheck(projectRoot.resolve("config.properties"), "config.properties present"));
        checks.add(fileCheck(projectRoot.resolve("docker-compose.yml"), "docker-compose.yml present"));
        checks.add(fileCheck(projectRoot.resolve("docker-compose.kafka.yml"), "docker-compose.kafka.yml present"));
        checks.add(fileCheck(projectRoot.resolve("server/seed-data.json"), "seed-data.json present"));
        checks.add(commandCheck("Docker CLI", new String[]{"docker", "--version"}));
        checks.add(commandCheck("Docker daemon", new String[]{"docker", "info"}));
        checks.add(commandCheck("Node.js", new String[]{"node", "--version"}));
        checks.add(commandCheck("npm", new String[]{"npm", "--version"}));
        checks.add(portCheck("Kafka localhost:9092", "localhost", 9092));
        checks.add(httpCheck("Mock API health endpoint", "http://localhost:3000/health"));
        return new EnvironmentReport(checks);
    }

    private EnvironmentCheck fileCheck(Path path, String label) {
        return Files.exists(path)
                ? new EnvironmentCheck(label, "PASS", path.toString())
                : new EnvironmentCheck(label, "WARN", "Missing " + path);
    }

    private EnvironmentCheck portCheck(String label, String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return new EnvironmentCheck(label, "PASS", "Reachable");
        } catch (IOException exception) {
            return new EnvironmentCheck(label, "WARN", exception.getMessage());
        }
    }

    private EnvironmentCheck httpCheck(String label, String urlValue) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(urlValue).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            int statusCode = connection.getResponseCode();
            return new EnvironmentCheck(label, statusCode == 200 ? "PASS" : "WARN", "HTTP " + statusCode);
        } catch (IOException exception) {
            return new EnvironmentCheck(label, "WARN", exception.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private EnvironmentCheck commandCheck(String label, String[] command) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean completed = process.waitFor(COMMAND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new EnvironmentCheck(label, "WARN", "Timed out");
            }

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().findFirst().orElse("No output");
            }

            return process.exitValue() == 0
                    ? new EnvironmentCheck(label, "PASS", output)
                    : new EnvironmentCheck(label, "WARN", output);
        } catch (Exception exception) {
            return new EnvironmentCheck(label, "WARN", exception.getMessage());
        }
    }
}
