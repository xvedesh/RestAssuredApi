package com.analysis.failure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FailureEvidence {
    private final String featureName;
    private final String featureUri;
    private final int scenarioLine;
    private final String scenarioName;
    private final List<String> tags;
    private final int failedStepLine;
    private final String failedStepKeyword;
    private final String failedStepText;
    private final String stepDefinitionLocation;
    private final String errorMessage;
    private final String rerunReference;
    private final String surefireSummary;

    public FailureEvidence(String featureName, String featureUri, int scenarioLine, String scenarioName,
                           List<String> tags, int failedStepLine, String failedStepKeyword, String failedStepText,
                           String stepDefinitionLocation, String errorMessage, String rerunReference,
                           String surefireSummary) {
        this.featureName = featureName;
        this.featureUri = featureUri;
        this.scenarioLine = scenarioLine;
        this.scenarioName = scenarioName;
        this.tags = new ArrayList<>(tags);
        this.failedStepLine = failedStepLine;
        this.failedStepKeyword = failedStepKeyword;
        this.failedStepText = failedStepText;
        this.stepDefinitionLocation = stepDefinitionLocation;
        this.errorMessage = errorMessage;
        this.rerunReference = rerunReference;
        this.surefireSummary = surefireSummary;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureUri() {
        return featureUri;
    }

    public int getScenarioLine() {
        return scenarioLine;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public int getFailedStepLine() {
        return failedStepLine;
    }

    public String getFailedStepKeyword() {
        return failedStepKeyword;
    }

    public String getFailedStepText() {
        return failedStepText;
    }

    public String getStepDefinitionLocation() {
        return stepDefinitionLocation;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getRerunReference() {
        return rerunReference;
    }

    public String getSurefireSummary() {
        return surefireSummary;
    }

    public String getFullFailedStep() {
        return (failedStepKeyword == null ? "" : failedStepKeyword.trim() + " ") + failedStepText;
    }
}
