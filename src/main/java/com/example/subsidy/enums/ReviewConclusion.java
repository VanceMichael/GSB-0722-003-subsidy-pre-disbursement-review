package com.example.subsidy.enums;

public enum ReviewConclusion {

    PASS("通过"),
    RETURN("退回");

    private final String description;

    ReviewConclusion(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
