package com.gov.subsidy.review.bootstrap;

import com.gov.subsidy.review.domain.entity.FlowRecord;
import com.gov.subsidy.review.domain.entity.SubsidyApplication;
import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import com.gov.subsidy.review.domain.enums.FlowAction;
import com.gov.subsidy.review.repository.FlowRecordRepository;
import com.gov.subsidy.review.repository.SubsidyApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 启动时写入少量可运行的样例数据，覆盖不同状态，便于演示各接口的状态前提。
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SubsidyApplicationRepository applicationRepository;
    private final FlowRecordRepository flowRecordRepository;

    @Override
    public void run(String... args) {
        if (applicationRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        // 1. 复审通过、可发起复核
        seed("阳光科技有限公司", "2026-Q1", new BigDecimal("120000.00"),
                ApplicationStatus.RE_REVIEW_PASSED, now);
        // 2. 已登记，尚未复审通过，不能发起复核
        seed("恒信物流有限公司", "2026-Q1", new BigDecimal("58000.50"),
                ApplicationStatus.REGISTERED, now);
        // 3. 已拨付，不能发起复核
        seed("大地建筑集团", "2025-Q4", new BigDecimal("300000.00"),
                ApplicationStatus.DISBURSED, now);
        // 4. 复审未通过，不能发起复核
        seed("未来教育科技", "2026-Q1", new BigDecimal("45000.00"),
                ApplicationStatus.RE_REVIEW_REJECTED, now);
    }

    private void seed(String employer, String period, BigDecimal amount,
                      ApplicationStatus status, LocalDateTime now) {
        SubsidyApplication app = applicationRepository.save(SubsidyApplication.builder()
                .employerName(employer)
                .subsidyPeriod(period)
                .appliedAmount(amount)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build());

        flowRecordRepository.save(FlowRecord.builder()
                .applicationId(app.getId())
                .action(FlowAction.REGISTER)
                .fromStatus(null)
                .toStatus(status)
                .operator("系统初始化")
                .remark("样例数据，初始状态：" + status.getLabel())
                .occurredAt(now)
                .build());
    }
}
