package com.example.subsidy.enums;

public enum ApplicationStatus {

    REGISTERED("已登记"),
    REVIEW_APPROVED("复审通过"),
    REVIEW_REJECTED("复审未通过"),
    PRE_REVIEWING("复核中"),
    PENDING_CORRECTION("待修正"),
    DISBURSED("已拨付");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
