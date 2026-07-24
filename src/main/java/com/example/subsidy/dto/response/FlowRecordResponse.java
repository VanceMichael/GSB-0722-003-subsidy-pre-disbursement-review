package com.example.subsidy.dto.response;

import com.example.subsidy.enums.ApplicationStatus;

import java.time.LocalDateTime;

public class FlowRecordResponse {

    private Long id;
    private ApplicationStatus fromStatus;
    private String fromStatusDescription;
    private ApplicationStatus toStatus;
    private String toStatusDescription;
    private String operation;
    private String operator;
    private String remark;
    private LocalDateTime operatedAt;

    public FlowRecordResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ApplicationStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ApplicationStatus fromStatus) { this.fromStatus = fromStatus; }
    public String getFromStatusDescription() { return fromStatusDescription; }
    public void setFromStatusDescription(String fromStatusDescription) { this.fromStatusDescription = fromStatusDescription; }
    public ApplicationStatus getToStatus() { return toStatus; }
    public void setToStatus(ApplicationStatus toStatus) { this.toStatus = toStatus; }
    public String getToStatusDescription() { return toStatusDescription; }
    public void setToStatusDescription(String toStatusDescription) { this.toStatusDescription = toStatusDescription; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getOperatedAt() { return operatedAt; }
    public void setOperatedAt(LocalDateTime operatedAt) { this.operatedAt = operatedAt; }
}
