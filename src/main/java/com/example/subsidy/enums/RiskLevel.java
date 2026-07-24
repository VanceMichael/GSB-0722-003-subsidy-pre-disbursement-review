package com.example.subsidy.enums;

public enum RiskLevel {

    LOW("低"),
    MEDIUM("中"),
    HIGH("高");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
