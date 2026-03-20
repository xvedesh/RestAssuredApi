package com.analysis.failure;

import org.testng.IExecutionListener;

import java.nio.file.Path;

public class FailureAnalysisExecutionListener implements IExecutionListener {
    @Override
    public void onExecutionFinish() {
        try {
            FailureAnalysisService.Result result = new FailureAnalysisService(
                    Path.of("").toAbsolutePath().normalize()
            ).generate("all");

            if (!result.getAnalyses().isEmpty()) {
                System.out.println("[AI-KAFKA-VALIDATOR][FailureAnalysis] Generated report for " + result.getAnalyses().size() + " failure(s).");
                System.out.println("[AI-KAFKA-VALIDATOR][FailureAnalysis] Markdown: " + result.getMarkdownPath());
                System.out.println("[AI-KAFKA-VALIDATOR][FailureAnalysis] HTML: " + result.getHtmlPath());
                System.out.println("[AI-KAFKA-VALIDATOR][FailureAnalysis] Surefire mirror: " + result.getSurefireHtmlPath());
                System.out.println("[AI-KAFKA-VALIDATOR][FailureAnalysis] Cucumber mirror: " + result.getCucumberHtmlPath());
            }
        } catch (Exception exception) {
            System.out.println("[AI-KAFKA-VALIDATOR][FailureAnalysis] Failed to generate automatic failure analysis: " + exception.getMessage());
        }
    }
}
