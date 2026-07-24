package com.gov.subsidy.review.dto;

import com.gov.subsidy.review.domain.entity.FlowRecord;
import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import com.gov.subsidy.review.domain.enums.FlowAction;

import java.time.LocalDateTime;

/** 流转记录响应。 */
public record FlowRecordResponse(
        Long id,
        Long applicationId,
        Long reviewId,
        FlowAction action,
        String actionLabel,
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        String operator,
        String remark,
        LocalDateTime occurredAt
) {
    public static FlowRecordResponse from(FlowRecord f) {
        return new FlowRecordResponse(
                f.getId(),
                f.getApplicationId(),
                f.getReviewId(),
                f.getAction(),
                f.getAction().getLabel(),
                f.getFromStatus(),
                f.getToStatus(),
                f.getOperator(),
                f.getRemark(),
                f.getOccurredAt()
        );
    }
}
