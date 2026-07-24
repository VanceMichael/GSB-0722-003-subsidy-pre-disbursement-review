package com.gov.subsidy.review.domain.entity;

import com.gov.subsidy.review.domain.enums.ReviewConclusion;
import com.gov.subsidy.review.domain.enums.ReviewStatus;
import com.gov.subsidy.review.domain.enums.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 复核实体。与申请分开建模，通过 applicationId 关联。
 */
@Entity
@Table(name = "subsidy_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubsidyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属申请 */
    @Column(nullable = false)
    private Long applicationId;

    /** 风险等级 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    /** 问题描述 */
    @Column(nullable = false, length = 1000)
    private String problemDescription;

    /** 处理结论：发起时可为空，做出结论时填写 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReviewConclusion conclusion;

    /** 处理人 */
    @Column(nullable = false, length = 100)
    private String handler;

    /** 复核状态：进行中/已关闭 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus reviewStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 关闭（做出结论）时间 */
    private LocalDateTime closedAt;
}
