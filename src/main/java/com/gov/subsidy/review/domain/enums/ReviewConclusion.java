package com.gov.subsidy.review.domain.enums;

/** 复核处理结论。 */
public enum ReviewConclusion {
    /** 通过，进入复核通过待拨付 */
    APPROVED("通过"),
    /** 退回，申请进入可修正状态 */
    RETURNED("退回");

    private final String label;

    ReviewConclusion(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
