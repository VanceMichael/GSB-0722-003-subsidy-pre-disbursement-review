package com.gov.subsidy.review.domain.enums;

/** 复核状态。用于保证一个申请同时只能有一个未关闭的复核。 */
public enum ReviewStatus {
    /** 进行中（未关闭） */
    OPEN("进行中"),
    /** 已关闭 */
    CLOSED("已关闭");

    private final String label;

    ReviewStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
