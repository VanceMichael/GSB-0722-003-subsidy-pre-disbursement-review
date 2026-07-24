package com.gov.subsidy.review.dto;

import com.gov.subsidy.review.domain.entity.SubsidyApplication;
import com.gov.subsidy.review.domain.enums.ApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 申请响应。 */
public record ApplicationResponse(
        Long id,
        String employerName,
        String subsidyPeriod,
        BigDecimal appliedAmount,
        ApplicationStatus status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ApplicationResponse from(SubsidyApplication a) {
        return new ApplicationResponse(
                a.getId(),
                a.getEmployerName(),
                a.getSubsidyPeriod(),
                a.getAppliedAmount(),
                a.getStatus(),
                a.getStatus().getLabel(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
