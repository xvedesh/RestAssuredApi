package com.utils;

import io.cucumber.java.Scenario;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TestLogger {
    private static final ThreadLocal<String> scenarioName = new ThreadLocal<>();
    private static final ThreadLocal<Scenario> cucumberScenario = new ThreadLocal<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String FRAMEWORK_PREFIX = "[AI-KAFKA-VALIDATOR]";

    private TestLogger() {
    }

    public static void setScenarioName(String name) {
        scenarioName.set(name);
    }

    public static void setCucumberScenario(Scenario scenario) {
        cucumberScenario.set(scenario);
    }

    public static void clear() {
        scenarioName.remove();
        cucumberScenario.remove();
    }

    public static void log(String message) {
        String currentScenario = scenarioName.get();
        String prefix = currentScenario == null ? FRAMEWORK_PREFIX + "[TEST]" : FRAMEWORK_PREFIX + "[TEST][" + currentScenario + "]";
        String finalMessage = LocalDateTime.now().format(FORMATTER) + " " + prefix + " " + message;
        System.out.println(finalMessage);
    }

    public static void attach(String content, String name) {
        Scenario scenario = cucumberScenario.get();
        if (scenario != null) {
            scenario.attach(content.getBytes(StandardCharsets.UTF_8), "text/plain", name);
        }
    }
}
