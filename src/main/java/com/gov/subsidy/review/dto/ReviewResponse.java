package com.gov.subsidy.review.dto;

import com.gov.subsidy.review.domain.entity.SubsidyReview;
import com.gov.subsidy.review.domain.enums.ReviewConclusion;
import com.gov.subsidy.review.domain.enums.ReviewStatus;
import com.gov.subsidy.review.domain.enums.RiskLevel;

import java.time.LocalDateTime;

/** 复核响应。 */
public record ReviewResponse(
        Long id,
        Long applicationId,
        RiskLevel riskLevel,
        String problemDescription,
        ReviewConclusion conclusion,
        String handler,
        ReviewStatus reviewStatus,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {
    public static ReviewResponse from(SubsidyReview r) {
        return new ReviewResponse(
                r.getId(),
                r.getApplicationId(),
                r.getRiskLevel(),
                r.getProblemDescription(),
                r.getConclusion(),
                r.getHandler(),
                r.getReviewStatus(),
                r.getCreatedAt(),
                r.getClosedAt()
        );
    }
}
