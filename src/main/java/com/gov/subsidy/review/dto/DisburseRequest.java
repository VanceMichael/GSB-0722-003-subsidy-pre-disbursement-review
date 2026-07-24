package com.gov.subsidy.review.dto;

import jakarta.validation.constraints.NotBlank;

/** 拨付请求。 */
public record DisburseRequest(
        @NotBlank(message = "拨付经办人不能为空")
        String operator,

        String remark
) {
}
