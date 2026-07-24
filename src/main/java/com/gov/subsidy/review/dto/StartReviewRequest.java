package com.gov.subsidy.review.dto;

import com.gov.subsidy.review.domain.enums.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 发起复核请求。 */
public record StartReviewRequest(
        @NotNull(message = "风险等级不能为空")
        RiskLevel riskLevel,

        @NotBlank(message = "问题描述不能为空")
        String problemDescription,

        @NotBlank(message = "处理人不能为空")
        String handler
) {
}
