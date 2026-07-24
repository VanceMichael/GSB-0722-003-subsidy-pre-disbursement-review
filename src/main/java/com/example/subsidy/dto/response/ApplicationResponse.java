package com.example.subsidy.dto.response;

import com.example.subsidy.enums.ApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private String unitName;
    private String subsidyPeriod;
    private BigDecimal amount;
    private ApplicationStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplicationResponse() {
    }

    public static ApplicationResponse of(Long id, String unitName, String subsidyPeriod,
                                         BigDecimal amount, ApplicationStatus status,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.id = id;
        resp.unitName = unitName;
        resp.subsidyPeriod = subsidyPeriod;
        resp.amount = amount;
        resp.status = status;
        resp.statusDescription = status != null ? status.getDescription() : null;
        resp.createdAt = createdAt;
        resp.updatedAt = updatedAt;
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getSubsidyPeriod() { return subsidyPeriod; }
    public void setSubsidyPeriod(String subsidyPeriod) { this.subsidyPeriod = subsidyPeriod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
