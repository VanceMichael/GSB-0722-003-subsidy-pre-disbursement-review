package com.gov.subsidy.review.domain.enums;

/** 流转记录动作类型。 */
public enum FlowAction {
    REGISTER("登记申请"),
    RE_REVIEW_PASS("复审通过"),
    RE_REVIEW_REJECT("复审未通过"),
    START_REVIEW("发起复核"),
    REVIEW_APPROVED("复核通过"),
    REVIEW_RETURNED("复核退回"),
    DISBURSE("拨付");

    private final String label;

    FlowAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
