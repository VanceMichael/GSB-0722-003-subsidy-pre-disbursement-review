package com.example.subsidy.enums;

public enum ReviewStatus {

    IN_PROGRESS("进行中"),
    CLOSED("已关闭");

    private final String description;

    ReviewStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
