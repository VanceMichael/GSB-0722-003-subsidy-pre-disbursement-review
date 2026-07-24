package com.example.subsidy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewInitiateRequest {

    @NotBlank(message = "发起人不能为空")
    @Size(max = 100, message = "发起人长度不能超过100")
    private String initiatedBy;

    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }
}
