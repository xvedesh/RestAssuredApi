package com.analysis.failure;

import com.analysis.failure.model.FailureEvidence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CucumberFailureParser {
    private final Path projectRoot;

    public CucumberFailureParser(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public List<FailureEvidence> parse() throws IOException {
        Path cucumberJsonPath = projectRoot.resolve("target/cucumber.json");
        if (!Files.exists(cucumberJsonPath)) {
            return Collections.emptyList();
        }

        String rerunContent = readIfExists(projectRoot.resolve("target/rerun.txt"));
        String surefireSummary = readIfExists(projectRoot.resolve("target/surefire-reports/TestSuite.txt"));
        JSONArray features = new JSONArray(Files.readString(cucumberJsonPath, StandardCharsets.UTF_8));
        List<FailureEvidence> failures = new ArrayList<>();

        for (int featureIndex = 0; featureIndex < features.length(); featureIndex++) {
            JSONObject feature = features.getJSONObject(featureIndex);
            JSONArray scenarios = feature.optJSONArray("elements");
            if (scenarios == null) {
                continue;
            }

            for (int scenarioIndex = 0; scenarioIndex < scenarios.length(); scenarioIndex++) {
                JSONObject scenario = scenarios.getJSONObject(scenarioIndex);
                FailureEvidence failure = parseFailedScenario(feature, scenario, rerunContent, surefireSummary);
                if (failure != null) {
                    failures.add(failure);
                }
            }
        }

        return failures;
    }

    private FailureEvidence parseFailedScenario(JSONObject feature, JSONObject scenario,
                                                String rerunContent, String surefireSummary) {
        JSONArray steps = scenario.optJSONArray("steps");
        if (steps == null) {
            return null;
        }

        for (int stepIndex = 0; stepIndex < steps.length(); stepIndex++) {
            JSONObject step = steps.getJSONObject(stepIndex);
            JSONObject result = step.optJSONObject("result");
            if (result == null) {
                continue;
            }

            String status = result.optString("status", "");
            if ("failed".equalsIgnoreCase(status) || "ambiguous".equalsIgnoreCase(status)
                    || "undefined".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
                return new FailureEvidence(
                        feature.optString("name", "Unknown Feature"),
                        normalizeUri(feature.optString("uri", "")),
                        scenario.optInt("line", -1),
                        scenario.optString("name", "Unknown Scenario"),
                        extractTags(scenario.optJSONArray("tags")),
                        step.optInt("line", -1),
                        step.optString("keyword", "").trim(),
                        step.optString("name", ""),
                        extractStepLocation(step.optJSONObject("match")),
                        result.optString("error_message", "").trim(),
                        buildRerunReference(rerunContent, feature.optString("uri", ""), scenario.optInt("line", -1)),
                        summarizeSurefire(surefireSummary)
                );
            }
        }

        return null;
    }

    private List<String> extractTags(JSONArray tagsArray) {
        if (tagsArray == null) {
            return Collections.emptyList();
        }

        List<String> tags = new ArrayList<>();
        for (int tagIndex = 0; tagIndex < tagsArray.length(); tagIndex++) {
            tags.add(tagsArray.getJSONObject(tagIndex).optString("name", ""));
        }
        return tags;
    }

    private String extractStepLocation(JSONObject match) {
        if (match == null) {
            return "";
        }
        return match.optString("location", "");
    }

    private String buildRerunReference(String rerunContent, String featureUri, int scenarioLine) {
        if (rerunContent == null || rerunContent.isBlank()) {
            return "";
        }

        String plainUri = normalizeUri(featureUri);
        String expectedToken = plainUri + ":" + scenarioLine;
        return rerunContent.contains(expectedToken) ? expectedToken : "";
    }

    private String summarizeSurefire(String surefireSummary) {
        if (surefireSummary == null || surefireSummary.isBlank()) {
            return "";
        }

        String[] lines = surefireSummary.split("\\R");
        StringBuilder builder = new StringBuilder();
        int added = 0;
        for (String line : lines) {
            if (!line.isBlank()) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(line);
                added++;
            }
            if (added == 3) {
                break;
            }
        }
        return builder.toString();
    }

    private String readIfExists(Path path) throws IOException {
        return Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : "";
    }

    private String normalizeUri(String uri) {
        if (uri == null) {
            return "";
        }
        return uri.startsWith("file:") ? uri.substring("file:".length()) : uri;
    }
}
