package com.gov.subsidy.review.domain.enums;

/**
 * 申请状态枚举。
 * 状态流转：
 * 已登记 -> 复审通过 -> 复核中 -> (退回) 可修正 / (通过) 复核通过待拨付 -> 已拨付
 * 复审未通过 与 已拨付 均为不可发起复核的终态。
 */
public enum ApplicationStatus {

    /** 已登记，尚未复审 */
    REGISTERED("已登记"),
    /** 复审通过，等待拨付，可发起复核 */
    RE_REVIEW_PASSED("复审通过"),
    /** 复核中，存在一个未关闭的复核 */
    UNDER_REVIEW("复核中"),
    /** 复核退回，进入可修正状态 */
    CORRECTABLE("可修正"),
    /** 复核通过，等待拨付 */
    APPROVED_FOR_DISBURSEMENT("复核通过待拨付"),
    /** 已拨付，不可再发起复核 */
    DISBURSED("已拨付"),
    /** 复审未通过，不可发起复核 */
    RE_REVIEW_REJECTED("复审未通过");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 是否允许发起复核：仅复审通过的申请可发起。 */
    public boolean canStartReview() {
        return this == RE_REVIEW_PASSED;
    }

    /** 是否可以进行复审：已登记或复核退回后可修正的申请可进入复审。 */
    public boolean canReReview() {
        return this == REGISTERED || this == CORRECTABLE;
    }

    /** 是否可以拨付：仅复核通过待拨付的申请可拨付。 */
    public boolean canDisburse() {
        return this == APPROVED_FOR_DISBURSEMENT;
    }
}
