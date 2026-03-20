package com.analysis.onboarding;

public enum QuestionIntent {
    QUICK_START,
    DOCKER_SETUP,
    LOCAL_SETUP,
    START_KAFKA_ONLY,
    RUN_NEGATIVE,
    RUN_KAFKA,
    RERUN_FAILURES,
    REPORTS,
    RESET_DATA,
    FAILURE_AGENT,
    DEMO_PATH,
    FRAMEWORK_OVERVIEW,
    KAFKA_FLOW,
    PROJECT_NAVIGATION,
    TROUBLESHOOT_KAFKA,
    TROUBLESHOOT_SERVER,
    TROUBLESHOOT_REPORTS,
    ENTERPRISE_KAFKA,
    ENTERPRISE_CICD,
    ENTERPRISE_BACKEND,
    ENTERPRISE_DATABASE,
    UNKNOWN;

    public static QuestionIntent classify(String question) {
        String normalized = question == null ? "" : question.toLowerCase();

        if (containsAny(normalized, "quick start", "how do i start", "how do i begin", "how do i start?") ) {
            return QUICK_START;
        }
        if (containsAny(normalized, "demo", "fastest demo", "show this project", "demo flow")) {
            return DEMO_PATH;
        }
        if (containsAny(normalized, "docker") && containsAny(normalized, "run", "start", "setup", "compose")) {
            return DOCKER_SETUP;
        }
        if (containsAny(normalized, "start only kafka", "kafka only", "start kafka only")) {
            return START_KAFKA_ONLY;
        }
        if (normalized.contains("kafka") && containsAny(normalized, "can't", "cannot", "unreachable", "not found", "why")) {
            return TROUBLESHOOT_KAFKA;
        }
        if (containsAny(normalized, "run locally", "local setup", "locally", "hybrid local")) {
            return LOCAL_SETUP;
        }
        if (containsAny(normalized, "negative scenario", "negative scenarios", "negative tests", "@negative")) {
            return RUN_NEGATIVE;
        }
        if (containsAny(normalized, "transaction kafka", "account kafka", "portfolio kafka", "client kafka", "kafka tests", "kafka scenarios")) {
            return RUN_KAFKA;
        }
        if (containsAny(normalized, "rerun failures", "rerun failed", "failed scenarios")) {
            return RERUN_FAILURES;
        }
        if (containsAny(normalized, "report", "reports", "pretty report", "surefire", "cucumber report")) {
            return REPORTS;
        }
        if (containsAny(normalized, "reset data", "seed", "seed-data", "restore data")) {
            return RESET_DATA;
        }
        if (containsAny(normalized, "failure analysis", "failure agent")) {
            return FAILURE_AGENT;
        }
        if (containsAny(normalized, "how does kafka", "where event", "publishes kafka", "publish account event", "kafka publishing", "kafka validation")) {
            return KAFKA_FLOW;
        }
        if (containsAny(normalized, "project structure", "how framework works", "how does the framework work", "framework structure", "understand framework")) {
            return FRAMEWORK_OVERVIEW;
        }
        if (containsAny(normalized, "where is", "which file", "where are", "navigate project", "navigation")) {
            return PROJECT_NAVIGATION;
        }
        if (containsAny(normalized, "api not starting", "server not starting", "cannot start server", "can't start server", "server unreachable")) {
            return TROUBLESHOOT_SERVER;
        }
        if (containsAny(normalized, "report empty", "reports empty", "why is the report empty")) {
            return TROUBLESHOOT_REPORTS;
        }
        if (containsAny(normalized, "real kafka", "kafka cluster", "multiple brokers", "sasl", "ssl", "topic naming", "enterprise kafka", "real broker")) {
            return ENTERPRISE_KAFKA;
        }
        if (containsAny(normalized, "jenkins", "github actions", "azure devops", "ci/cd", "ci pipeline", "ci cd")) {
            return ENTERPRISE_CICD;
        }
        if (containsAny(normalized, "real backend", "json-server", "adapt to real backend", "real api", "real service")) {
            return ENTERPRISE_BACKEND;
        }
        if (containsAny(normalized, "database", "db", "seeded environment", "test environment")) {
            return ENTERPRISE_DATABASE;
        }
        return UNKNOWN;
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
