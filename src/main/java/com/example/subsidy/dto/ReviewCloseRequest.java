package com.example.subsidy.dto;

import com.example.subsidy.enums.ReviewConclusion;
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
public class ReviewCloseRequest {

    @NotNull(message = "处理结论不能为空")
    private ReviewConclusion conclusion;

    private String problemDescription;

    @NotBlank(message = "处理人不能为空")
    private String processor;
}
