package com.gov.subsidy.review.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 登记申请请求。 */
public record CreateApplicationRequest(
        @NotBlank(message = "单位名称不能为空")
        String employerName,

        @NotBlank(message = "补贴周期不能为空")
        String subsidyPeriod,

        @NotNull(message = "申请金额不能为空")
        @DecimalMin(value = "0.01", message = "申请金额必须大于0")
        BigDecimal appliedAmount
) {
}
