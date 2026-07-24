package com.gov.subsidy.review.domain.entity;

import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补贴申请实体。
 */
@Entity
@Table(name = "subsidy_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubsidyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 单位名称 */
    @Column(nullable = false, length = 200)
    private String employerName;

    /** 补贴周期，如 2026-Q1 */
    @Column(nullable = false, length = 50)
    private String subsidyPeriod;

    /** 申请金额，使用 BigDecimal */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal appliedAmount;

    /** 当前状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 乐观锁，防止并发下重复发起复核 */
    @Version
    private Long version;
}
