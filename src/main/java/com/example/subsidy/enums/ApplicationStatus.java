package com.example.subsidy.enums;

public enum ApplicationStatus {

    REGISTERED("已登记"),
    UNDER_REVIEW("复审中"),
    REVIEW_APPROVED("复审通过"),
    PRE_DISBURSEMENT_REVIEW("拨付前复核中"),
    RETURNED_FOR_CORRECTION("复核退回"),
    DISBURSED("已拨付"),
    REVIEW_REJECTED("复审未通过");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
