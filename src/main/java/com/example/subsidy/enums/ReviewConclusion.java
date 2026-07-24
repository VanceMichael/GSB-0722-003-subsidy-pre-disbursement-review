package com.example.subsidy.enums;

public enum ReviewConclusion {

    PASS("复核通过"),
    RETURN("退回修正");

    private final String description;

    ReviewConclusion(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
