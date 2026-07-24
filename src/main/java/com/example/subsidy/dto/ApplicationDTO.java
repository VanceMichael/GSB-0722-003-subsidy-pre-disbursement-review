package com.example.subsidy.dto;

import com.example.subsidy.entity.SubsidyApplication;
import com.example.subsidy.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {

    private Long id;
    private String unitName;
    private String subsidyPeriod;
    private BigDecimal amount;
    private ApplicationStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApplicationDTO from(SubsidyApplication app) {
        return ApplicationDTO.builder()
                .id(app.getId())
                .unitName(app.getUnitName())
                .subsidyPeriod(app.getSubsidyPeriod())
                .amount(app.getAmount())
                .status(app.getStatus())
                .statusDescription(app.getStatus().getDescription())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
