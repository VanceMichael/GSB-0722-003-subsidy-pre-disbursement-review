package com.example.subsidy.dto.request;

import jakarta.validation.constraints.Size;

public class ApplicationStatusUpdateRequest {

    @Size(max = 100, message = "操作人长度不能超过100")
    private String operator;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
