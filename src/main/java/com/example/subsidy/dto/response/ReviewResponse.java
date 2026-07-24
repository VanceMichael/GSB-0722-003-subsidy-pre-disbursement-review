package com.example.subsidy.dto.response;

import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.enums.RiskLevel;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long applicationId;
    private ReviewStatus status;
    private String statusDescription;
    private RiskLevel riskLevel;
    private String riskLevelDescription;
    private String problemDescription;
    private ReviewConclusion conclusion;
    private String conclusionDescription;
    private String handler;
    private String initiatedBy;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;

    public ReviewResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskLevelDescription() { return riskLevelDescription; }
    public void setRiskLevelDescription(String riskLevelDescription) { this.riskLevelDescription = riskLevelDescription; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public ReviewConclusion getConclusion() { return conclusion; }
    public void setConclusion(ReviewConclusion conclusion) { this.conclusion = conclusion; }
    public String getConclusionDescription() { return conclusionDescription; }
    public void setConclusionDescription(String conclusionDescription) { this.conclusionDescription = conclusionDescription; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
