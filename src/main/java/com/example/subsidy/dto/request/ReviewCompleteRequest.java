package com.example.subsidy.dto.request;

import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.RiskLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewCompleteRequest {

    @NotNull(message = "风险等级不能为空")
    private RiskLevel riskLevel;

    @Size(max = 1000, message = "问题描述长度不能超过1000")
    private String problemDescription;

    @NotNull(message = "处理结论不能为空")
    private ReviewConclusion conclusion;

    @NotNull(message = "处理人不能为空")
    @Size(max = 100, message = "处理人长度不能超过100")
    private String handler;

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public ReviewConclusion getConclusion() { return conclusion; }
    public void setConclusion(ReviewConclusion conclusion) { this.conclusion = conclusion; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
}
