package com.example.subsidy.dto;

import com.example.subsidy.enums.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "风险等级不能为空")
    private RiskLevel riskLevel;

    private String problemDescription;

    @NotBlank(message = "处理人不能为空")
    private String processor;
}
