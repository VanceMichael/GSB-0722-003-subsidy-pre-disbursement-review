package com.example.subsidy.enums;

public enum ReviewStatus {

    OPEN("未关闭"),
    CLOSED("已关闭");

    private final String description;

    ReviewStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
