package com.analysis.failure.model;

public class CodeReference {
    private final String label;
    private final String path;
    private final Integer line;
    private final String detail;

    public CodeReference(String label, String path, Integer line, String detail) {
        this.label = label;
        this.path = path;
        this.line = line;
        this.detail = detail;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    public Integer getLine() {
        return line;
    }

    public String getDetail() {
        return detail;
    }
}
