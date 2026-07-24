package com.example.subsidy.dto;

import com.example.subsidy.entity.PreDisbursementReview;
import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long id;
    private Long applicationId;
    private RiskLevel riskLevel;
    private String riskLevelDescription;
    private String problemDescription;
    private ReviewConclusion conclusion;
    private String conclusionDescription;
    private String processor;
    private ReviewStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public static ReviewDTO from(PreDisbursementReview review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .applicationId(review.getApplication().getId())
                .riskLevel(review.getRiskLevel())
                .riskLevelDescription(review.getRiskLevel().getDescription())
                .problemDescription(review.getProblemDescription())
                .conclusion(review.getConclusion())
                .conclusionDescription(review.getConclusion() != null ? review.getConclusion().getDescription() : null)
                .processor(review.getProcessor())
                .status(review.getStatus())
                .statusDescription(review.getStatus().getDescription())
                .createdAt(review.getCreatedAt())
                .closedAt(review.getClosedAt())
                .build();
    }
}
