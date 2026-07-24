package com.gov.subsidy.review.domain.enums;

/** 风险等级。 */
public enum RiskLevel {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高");

    private final String label;

    RiskLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
