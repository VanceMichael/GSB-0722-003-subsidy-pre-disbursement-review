package com.gov.subsidy.review.domain.entity;

import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import com.gov.subsidy.review.domain.enums.FlowAction;
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
 * 流转记录实体。每一次状态变化都会留下一笔完整记录。
 */
@Entity
@Table(name = "subsidy_flow_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属申请 */
    @Column(nullable = false)
    private Long applicationId;

    /** 关联复核（若有） */
    private Long reviewId;

    /** 动作类型 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FlowAction action;

    /** 变化前状态 */
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ApplicationStatus fromStatus;

    /** 变化后状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus toStatus;

    /** 操作人 */
    @Column(nullable = false, length = 100)
    private String operator;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
