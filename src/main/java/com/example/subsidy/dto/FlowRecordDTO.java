package com.example.subsidy.dto;

import com.example.subsidy.entity.ApplicationFlowRecord;
import com.example.subsidy.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowRecordDTO {

    private Long id;
    private ApplicationStatus fromStatus;
    private String fromStatusDescription;
    private ApplicationStatus toStatus;
    private String toStatusDescription;
    private String action;
    private String remark;
    private String operator;
    private LocalDateTime createdAt;

    public static FlowRecordDTO from(ApplicationFlowRecord record) {
        return FlowRecordDTO.builder()
                .id(record.getId())
                .fromStatus(record.getFromStatus())
                .fromStatusDescription(record.getFromStatus() != null ? record.getFromStatus().getDescription() : null)
                .toStatus(record.getToStatus())
                .toStatusDescription(record.getToStatus().getDescription())
                .action(record.getAction())
                .remark(record.getRemark())
                .operator(record.getOperator())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
