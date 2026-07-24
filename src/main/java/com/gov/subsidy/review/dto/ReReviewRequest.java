package com.gov.subsidy.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 复审请求：决定申请是否复审通过。 */
public record ReReviewRequest(
        @NotNull(message = "复审是否通过不能为空")
        Boolean passed,

        @NotBlank(message = "复审人不能为空")
        String operator,

        String remark
) {
}
