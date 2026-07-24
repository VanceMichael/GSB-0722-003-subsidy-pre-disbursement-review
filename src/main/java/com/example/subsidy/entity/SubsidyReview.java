package com.example.subsidy.entity;

import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.enums.RiskLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subsidy_review")
public class SubsidyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private SubsidyApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReviewStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel;

    @Column(name = "problem_description", length = 1000)
    private String problemDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "conclusion", length = 20)
    private ReviewConclusion conclusion;

    @Column(name = "handler", length = 100)
    private String handler;

    @Column(name = "initiated_by", length = 100)
    private String initiatedBy;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public SubsidyReview() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.initiatedAt == null) {
            this.initiatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ReviewStatus.IN_PROGRESS;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SubsidyApplication getApplication() { return application; }
    public void setApplication(SubsidyApplication application) { this.application = application; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public ReviewConclusion getConclusion() { return conclusion; }
    public void setConclusion(ReviewConclusion conclusion) { this.conclusion = conclusion; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
