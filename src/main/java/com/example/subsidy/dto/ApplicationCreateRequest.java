package com.example.subsidy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreateRequest {

    @NotBlank(message = "单位名称不能为空")
    private String unitName;

    @NotBlank(message = "补贴周期不能为空")
    private String subsidyPeriod;

    @NotNull(message = "申请金额不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "申请金额必须大于0")
    private BigDecimal amount;
}
