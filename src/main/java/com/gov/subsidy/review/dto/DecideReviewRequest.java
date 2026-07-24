package com.gov.subsidy.review.dto;

import com.gov.subsidy.review.domain.enums.ReviewConclusion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 做出复核结论请求（通过或退回）。 */
public record DecideReviewRequest(
        @NotNull(message = "处理结论不能为空")
        ReviewConclusion conclusion,

        @NotBlank(message = "处理人不能为空")
        String handler,

        String remark
) {
}
